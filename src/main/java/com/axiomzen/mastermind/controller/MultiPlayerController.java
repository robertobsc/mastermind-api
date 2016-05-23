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
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.axiomzen.mastermind.controller.validator.MultiPlayerValidator;
import com.axiomzen.mastermind.domain.Guess;
import com.axiomzen.mastermind.domain.MastermindGame;
import com.axiomzen.mastermind.domain.User;
import com.axiomzen.mastermind.domain.interfaces.GameRule;
import com.axiomzen.mastermind.domain.to.GuessReqTO;

/**
 * Multiplayer version of Mastermind game.
 * @author Roberto Costa
 */
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
	
	@RequestMapping(value = "/new_game", 
			consumes = "application/x-www-form-urlencoded;charset=UTF-8", 
			method = RequestMethod.POST)
	public ResponseEntity<ResponseBodyEmitter> startMultiPlayerGameFromPage(@Valid User user, Integer numPlayers, 
			Errors errors) throws IOException{
		
		return startMultiPlayerGame(user, numPlayers, errors);
	}
	
	@RequestMapping(value = "/new_game", consumes = "application/json", method = RequestMethod.POST)
	public ResponseEntity<ResponseBodyEmitter> startMultiPlayerGame(@Valid User user,
			Integer numPlayers, Errors errors) throws IOException{

		logger.debug(">> startMultiPlayerGame");
		
	    ResponseEntity<ResponseBodyEmitter> respEntity = createJsonResponseBodyEmitter();
	    ResponseBodyEmitter emitter = respEntity.getBody();
		MastermindGame game = null;
		try {
			if (errors.hasErrors()) {
				responseEmitterHelper.emitValidationErrors(emitter, "", "", errors);
			} else {
				int numOfPlayers = 
						(numPlayers == null || numPlayers <= 1) ? 2 : numPlayers; 
				User userSaved = User.save(user);
				
				game = MastermindGame.create(userSaved, numOfPlayers, mastermindRule);
				
				responseEmitterHelper.addGameEmitter(game.getKey(), emitter);
				responseEmitterHelper.emitUserKey(emitter, game, userSaved.getKey());
				responseEmitterHelper.emitStartGameResponse(emitter, errors, game);
			}
			
	    
		} catch(Exception e) {
			logger.error("Error while starting multiplayer game: ", e);

			responseEmitterHelper.emitExceptionResponse(emitter, "creating the game", e);
		} finally {
			logger.debug("<< startMultiPlayerGame");
		}
		return respEntity;			
	}
	
	@RequestMapping(value = "/join_game", method = RequestMethod.GET)
	public String joinMultiPlayerGamePage() {
		return "joinGame";
	}
	
	@RequestMapping(value = "/join_game", method = RequestMethod.POST)
	public ResponseEntity<ResponseBodyEmitter> joinMultiPlayerGame(@Valid User joinUser, String gameKey, Errors errors) throws IOException{

		logger.debug(">> joinMultiPlayerGame({},{})", joinUser, gameKey);
		MastermindGame game = null;
		ResponseEntity<ResponseBodyEmitter> respEntity = createJsonResponseBodyEmitter();
		ResponseBodyEmitter emitter = respEntity.getBody();
		try {
			
			if (errors.hasErrors()) {
				responseEmitterHelper.emitValidationErrors(emitter, gameKey, "", errors);				
			} else {
				synchronized(this) {
					game = MastermindGame.get(gameKey);
					if (MultiPlayerValidator.checkPartyFull(gameKey, errors)){
						responseEmitterHelper.emitValidationErrors(emitter, gameKey, "", errors);
						return respEntity;
					}
					
					User userSaved = User.save(joinUser);
					game.getUsers().add(userSaved);
					game = MastermindGame.save(game);
					
					responseEmitterHelper.addGameEmitter(gameKey, emitter);
					responseEmitterHelper.emitUserKey(emitter, game, userSaved.getKey());
					if (game.getUsers().size() == game.getNumberOfUsers()) {
						responseEmitterHelper.emitPartyCompletedMessageForAll(game);
					} else { 
						responseEmitterHelper.emitUserJoinedMessageForAll(game);
					}					
				}
			}
		} catch(Exception e) {
			logger.error("Error while starting multiplayer game: ", e);
			responseEmitterHelper.emitExceptionResponse(emitter, "user joining the game", e);
		} finally {
			logger.debug("<< joinMultiPlayerGame");			
		}
		return respEntity;
	}
	
	@RequestMapping(value = "/guess", method = RequestMethod.GET)
	public String guessPage() {
		return "guess";
	}
	
	@RequestMapping(value = "/guess", 
			consumes = "application/x-www-form-urlencoded;charset=UTF-8", method = RequestMethod.POST)
	public synchronized ResponseEntity<ResponseBodyEmitter> guessForm(
			@Valid GuessReqTO guess, Errors errors) throws IOException {
		return guess(guess, errors);
	}
	
	@RequestMapping(value = "/guess", consumes = "application/json",method = RequestMethod.POST)
	public synchronized ResponseEntity<ResponseBodyEmitter> guess(
			@RequestBody @Valid GuessReqTO guessTO, Errors errors) throws IOException {

		logger.debug(">> guess");
		
		MastermindGame game = null;

		ResponseEntity<ResponseBodyEmitter> respEntity = createJsonResponseBodyEmitter();
		ResponseBodyEmitter emitter = (ResponseBodyEmitter) respEntity.getBody();
		
		try {
			String gameKey = guessTO == null ? "" : guessTO.getGame_key();
			String userKey = guessTO == null ? "" : guessTO.getUser_key();
			if (errors.hasErrors()) {				
				responseEmitterHelper.emitValidationErrors(emitter, gameKey, userKey, errors);
				
			} else {
				logger.debug("user key: {}, game key {}", userKey, gameKey);
				
				Guess guess = Guess.fromGuessRequestTO(guessTO);
				game = MastermindGame.doNewGuess(guess);
	
				responseEmitterHelper.addGameEmitter(guess.getGameKey(), emitter);
				if (game.isEndOfTurn()) {
					responseEmitterHelper.emitEndOfTurnForAll(game);
				} else {		
					responseEmitterHelper.emitPlayerGuessedMessageForAll(game);
				}
			}
		} catch(Exception e) {
			logger.error("Error while guessing multiplayer game: ", e);
			responseEmitterHelper.emitExceptionResponse(emitter, "making this guess", e);
		} finally {
			logger.debug("<< guess");	
		}
		return respEntity;
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
}
