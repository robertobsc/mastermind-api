package com.axiomzen.mastermind.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.axiomzen.mastermind.controller.validator.ValidationErrosBuilder;
import com.axiomzen.mastermind.domain.Guess;
import com.axiomzen.mastermind.domain.MastermindGame;
import com.axiomzen.mastermind.domain.User;
import com.axiomzen.mastermind.domain.interfaces.GameRule;
import com.axiomzen.mastermind.domain.to.GuessReqTO;

@RestController
@RequestMapping("/multiplayer")
public class MultiPlayerController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	Validator guessRequestValidator;
	
	@Autowired
	Validator createGameValidator;
	
	@Autowired
	@Qualifier("mastermindRule")
	GameRule mastermindRule;
	
	//@Autowired
	//AsyncTaskExecutor executor;
	
	@Autowired
	ResponseEmitterHelper responseEmitterHelper;
	
	private static Map<String, ResponseBodyEmitter> gameEmitter = new HashMap<>();
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
	    Arrays.asList(guessRequestValidator, createGameValidator)
	    .forEach( v -> {
	        if (binder.getTarget() != null && v.supports(binder.getTarget().getClass())) {
	            binder.addValidators(v);
	        }});   
	}
	
	@RequestMapping(value = "/new_game", consumes = "application/json",method = RequestMethod.POST)
	public ResponseEntity<ResponseBodyEmitter> startMultiPlayerGame(@RequestBody @Valid User user, 
			Errors errors) throws IOException{

		logger.debug(">> startMultiPlayerGame");
		
	    ResponseEntity<ResponseBodyEmitter> respEntity = createJsonResponseBodyEmitter();
	    ResponseBodyEmitter emitter = respEntity.getBody();
		MastermindGame game = null;
		try {
			if (!errors.hasErrors()) {
				User userSaved = User.save(user);
				game = MastermindGame.create(user, mastermindRule);
				
				gameEmitter.put(userSaved.getKey() + game.getKey(), emitter);
			}
			responseEmitterHelper.emitStartGameResponse(emitter, errors, game);
	    
		} catch(Exception e) {
			logger.error("Error while starting multiplayer game: ", e);
			emitter.send("Ops! Unexpected error while creating the game: " + e.getMessage());
		}
		
		
		logger.debug("<< startMultiPlayerGame");
	    return respEntity;
	}
	
	@RequestMapping(value = "/guess", consumes = "application/json",method = RequestMethod.POST)
	public ResponseEntity guess(@RequestBody @Valid GuessReqTO guess, Errors errors) {
	
		try {
	        if (errors.hasErrors()) {
	            return ResponseEntity.badRequest().body(
	            		ValidationErrosBuilder.build(errors));
	        }			
			
			return ResponseEntity.ok(
					MastermindGame.doNewGuess(
							Guess.fromGuessRequestTO(guess)));
		} catch(Exception e) {
			return ResponseEntity.badRequest().body(
					"Ops! Unexpected error while making this guess: " + e.getMessage());
		}
	}
	
	
	
	@RequestMapping(value="/start-talk", method=RequestMethod.GET)
	public ResponseEntity<ResponseBodyEmitter> handle(@RequestParam("gameKey") String gameKey) throws IOException {
		logger.debug(">> start-talk");
		
		ResponseBodyEmitter emitter = new ResponseBodyEmitter();
		gameEmitter.put(gameKey, emitter);

		responseEmitterHelper.asyncResponse(emitter, gameKey);
		//CompletableFuture.supplyAsync(() -> {
		//	return asyncResponse(emitter, gameKey);
		//});
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json; charset=UTF-8");
	    headers.add("Transfer-Encoding", "chunked");
		logger.debug("<< start-talk");
	    return (new ResponseEntity<ResponseBodyEmitter>(emitter, headers, HttpStatus.OK));
	}
	
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
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json; charset=UTF-8");
	    headers.add("Transfer-Encoding", "chunked");
	    return new ResponseEntity<ResponseBodyEmitter>(emitter, headers, HttpStatus.OK);
	}

	@RequestMapping(value= "/send-text", method = RequestMethod.GET)
	public String sendText(@RequestParam("text") String text, @RequestParam("gameKey") String gameKey) throws IOException {
		
		gameEmitter.get(gameKey).send(text);
		gameEmitter.get(gameKey).complete();
		
		return "message Sent";
	}
}
