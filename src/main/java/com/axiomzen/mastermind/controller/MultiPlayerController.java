package com.axiomzen.mastermind.controller;

import java.io.IOException;
import java.util.Arrays;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.axiomzen.mastermind.controller.validator.MultiPlayerValidator;
import com.axiomzen.mastermind.controller.validator.ValidationErrosBuilder;
import com.axiomzen.mastermind.domain.Guess;
import com.axiomzen.mastermind.domain.MastermindGame;
import com.axiomzen.mastermind.domain.User;
import com.axiomzen.mastermind.domain.interfaces.GameRule;
import com.axiomzen.mastermind.domain.to.GuessReqTO;

@Controller
@RequestMapping("/multiplayer")
public class MultiPlayerController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	Validator guessRequestMultiPlayerValidator;
	
	@Autowired
	Validator createGameValidator;
	
	@Autowired
	@Qualifier("mastermindRule")
	GameRule mastermindRule;
	
	@Autowired
	ResponseEmitterHelper responseEmitterHelper;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
	    Arrays.asList(guessRequestMultiPlayerValidator, createGameValidator)
	    .forEach( v -> {
	        if (binder.getTarget() != null && v.supports(binder.getTarget().getClass())) {
	            binder.addValidators(v);
	        }});   
	}
	
	@RequestMapping(value = "/new_game", method = RequestMethod.GET)
	public String getNewGamePage(){
		return "newGame";
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/new_game", 
			consumes = "application/x-www-form-urlencoded;charset=UTF-8", 
			method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity startMultiPlayerGameFromPage(@Valid User user, Integer numPlayers, 
			Errors errors) throws IOException{
		
		return startMultiPlayerGame(user, numPlayers, errors);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/new_game", consumes = "application/json", method = RequestMethod.POST)
	public ResponseEntity<ResponseBodyEmitter> startMultiPlayerGame(@Valid User user,
			Integer numPlayers, Errors errors) throws IOException{

		logger.debug(">> startMultiPlayerGame");
		
	    ResponseEntity<ResponseBodyEmitter> respEntity = createJsonResponseBodyEmitter();
	    ResponseBodyEmitter emitter = respEntity.getBody();
		MastermindGame game = null;
		try {
			if (errors.hasErrors()) {
//				return new ResponseEntity(ValidationErrosBuilder.build(errors),
//						getHttpHeaders(), HttpStatus.BAD_REQUEST);	
				responseEmitterHelper.emitValidationErrors(emitter, "", "", errors);
			} else {
				int numOfPlayers = 
						(numPlayers == null || numPlayers <= 1) ? 2 : numPlayers; 
				User userSaved = User.save(user);
				
				game = MastermindGame.create(userSaved, numOfPlayers, mastermindRule);
				
				responseEmitterHelper.addGameEmitter(game.getKey(), emitter);
				responseEmitterHelper.emitStartGameResponse(emitter, errors, game);
			}
			
	    
		} catch(Exception e) {
			logger.error("Error while starting multiplayer game: ", e);

			responseEmitterHelper.emitExceptionResponse(emitter, "creating the game", e);
//			return ResponseEntity.badRequest().body(
//					MessageGameFlowHelper.getException("creating the game", e));
		} finally {
			logger.debug("<< startMultiPlayerGame");
		}
		return respEntity;			
	}
	
	@RequestMapping(value = "/join_game", method = RequestMethod.GET)
	public String joinMultiPlayerGamePage() {
		return "joinGame";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/join_game", method = RequestMethod.POST)
	public ResponseEntity joinMultiPlayerGame(@Valid User joinUser, String gameKey, Errors errors) throws IOException{

		logger.debug(">> joinMultiPlayerGame({},{})", joinUser, gameKey);
		MastermindGame game = null;
		try {
			
			if (errors.hasErrors()) {
				return new ResponseEntity(ValidationErrosBuilder.build(gameKey, errors),
						getHttpHeaders(), HttpStatus.BAD_REQUEST);				
			} else {
				synchronized(this) {
					game = MastermindGame.get(gameKey);
					if (MultiPlayerValidator.checkPartyFull(gameKey, errors)){
						return new ResponseEntity(ValidationErrosBuilder.build(gameKey, errors),
								getHttpHeaders(), HttpStatus.BAD_REQUEST);						
					}
					
					User userSaved = User.save(joinUser);
					game.getUsers().add(userSaved);
					MastermindGame.save(game);
					
					ResponseEntity respEntity = createJsonResponseBodyEmitter();
					responseEmitterHelper.addGameEmitter(gameKey, (ResponseBodyEmitter)respEntity.getBody());
					
					if (game.getUsers().size() == game.getNumberOfUsers()) {
						responseEmitterHelper.emitPartyCompletedMessageForAll(game);
						responseEmitterHelper.removeGameEmitters(gameKey);
					} else { 
						responseEmitterHelper.emitUserJoinedMessageForAll(game);
					}
					
					return respEntity;
				}
			}
		} catch(Exception e) {
			logger.error("Error while starting multiplayer game: ", e);
			return ResponseEntity.badRequest().body("Ops! Unexpected error while creating the game: " + e.getMessage());
		} finally {
			logger.debug("<< joinMultiPlayerGame");			
		}
	}
	
	@RequestMapping(value = "/guess", method = RequestMethod.GET)
	public String guessPage() {
		return "guess";
	}
	
	@RequestMapping(value = "/guess", 
			consumes = "application/x-www-form-urlencoded;charset=UTF-8", method = RequestMethod.POST)
	public synchronized ResponseEntity<ResponseBodyEmitter> guessForm(@Valid GuessReqTO guess, Errors errors) {
		return guess(guess, errors);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/guess", consumes = "application/json",method = RequestMethod.POST)
	public synchronized ResponseEntity guess(@RequestBody @Valid GuessReqTO guessTO, Errors errors) {

		logger.debug(">> guess");
		
		MastermindGame game = null;
		
		try {
			if (errors.hasErrors()) {
				String gameKey = guessTO == null ? "" : guessTO.getGame_key();
				String userKey = guessTO == null ? "" : guessTO.getUser_key();
				
				return new ResponseEntity(
						ValidationErrosBuilder.build(gameKey, userKey, errors),
						getHttpHeaders(),
						HttpStatus.BAD_REQUEST);
			} 

			Guess guess = Guess.fromGuessRequestTO(guessTO);
			game = MastermindGame.doNewGuess(guess);

			ResponseEntity respEntity = createJsonResponseBodyEmitter();
			ResponseBodyEmitter emitter = (ResponseBodyEmitter) respEntity.getBody();
			responseEmitterHelper.addGameEmitter(guess.getGameKey(), emitter);
			if (game.isEndOfTurn()) {
				responseEmitterHelper.emitEndOfTurnForAll(game);
			} else {		
				responseEmitterHelper.emitPlayerGuessedMessageForAll(game);
			}
			
			return respEntity;

		} catch(Exception e) {
			logger.error("Error while guessing multiplayer game: ", e);
			return ResponseEntity.badRequest().body(
					MessageGameFlowHelper.getException("making this guess", e));
		} finally {
			logger.debug("<< guess");	
		}
	}

/*	@RequestMapping(value="/start-talk", method=RequestMethod.GET)
	public ResponseEntity<ResponseBodyEmitter> handle(@RequestParam("gameKey") String gameKey) throws IOException {
		logger.debug(">> start-talk");
		
		ResponseBodyEmitter emitter = new ResponseBodyEmitter();
		gameEmitter.put(gameKey, emitter);

		responseEmitterHelper.asyncResponse(emitter, gameKey);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json; charset=UTF-8");
	    headers.add("Transfer-Encoding", "chunked");
		logger.debug("<< start-talk");
	    return (new ResponseEntity<ResponseBodyEmitter>(emitter, headers, HttpStatus.OK));
	}*/
	
	private Void asyncResponse(ResponseBodyEmitter emitter, String gameKey){
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		
		try {
			emitter.send("Talk started with key " + gameKey);
			logger.debug("Emitter sent message.");
		} catch (IOException e) {
		}
		
		return null;
	}
	
	private static ResponseEntity<ResponseBodyEmitter> createJsonResponseBodyEmitter(){
		ResponseBodyEmitter emitter = new ResponseBodyEmitter();
	    return new ResponseEntity<ResponseBodyEmitter>(emitter, getHttpHeaders(), HttpStatus.OK);
	}

	private static HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=UTF-8");
		headers.add("Transfer-Encoding", "chunked");
		return headers;
		
	}
/*	@RequestMapping(value= "/send-text", method = RequestMethod.GET)
	public String sendText(@RequestParam("text") String text, @RequestParam("gameKey") String gameKey) throws IOException {
		
		gameEmitter.get(gameKey).send(text);
		gameEmitter.get(gameKey).complete();
		
		return "message Sent";
	}*/
}
