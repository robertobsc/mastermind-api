package com.axiomzen.mastermind.controller;

import java.util.Arrays;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.axiomzen.mastermind.controller.validator.ValidationErrosBuilder;
import com.axiomzen.mastermind.domain.Guess;
import com.axiomzen.mastermind.domain.MastermindGame;
import com.axiomzen.mastermind.domain.User;
import com.axiomzen.mastermind.domain.interfaces.GameRule;
import com.axiomzen.mastermind.domain.to.GuessReqTO;
import com.axiomzen.mastermind.domain.to.MessageTO;

/**
 * Singleplayer version of Mastermind game.
 * @author Roberto Costa
 */
@RestController
@RequestMapping("/singleplayer")
public class SinglePlayerController {

	//TODO: log
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	Validator guessRequestValidator;
	
	@Autowired
	Validator createGameValidator;
	
	@Autowired
	@Qualifier("mastermindRule")
	GameRule mastermindRule;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
	    Arrays.asList(guessRequestValidator, createGameValidator)
	    .forEach( v -> {
	        if (binder.getTarget() != null && v.supports(binder.getTarget().getClass())) {
	            binder.addValidators(v);
	        }});   
	}
	
	@RequestMapping(value = "/new_game", consumes = "application/json",method = RequestMethod.POST)
	public ResponseEntity<MessageTO> startGame(@RequestBody @Valid User user, Errors errors){
		
		try {
			if (errors.hasErrors()) {
				return ResponseEntity.badRequest().body(
						ValidationErrosBuilder.build(errors));
			}

			User savedUser = User.save(user);
			return ResponseEntity.ok(
					MastermindGame.create(savedUser, 1, mastermindRule)
					.toInitialGameResponseTO());
		} catch(Exception e) {
			return ResponseEntity.badRequest().body(
					MessageGameFlowHelper.getException("creating the game", e));
		}
	
	}
	
	@RequestMapping(value = "/guess", consumes = "application/json",method = RequestMethod.POST)
	public ResponseEntity<MessageTO> guess(@RequestBody @Valid GuessReqTO guess, Errors errors) {
	
		try {
	        if (errors.hasErrors()) {
	        	
	        	String gameKey = (guess != null && guess.getGame_key() != null) ?
	        			guess.getGame_key() : "";
	        			
	            return ResponseEntity.badRequest().body(
	            		ValidationErrosBuilder.build(gameKey, errors));
	        }			
			
			return ResponseEntity.ok(
					MastermindGame
						.doNewGuess(Guess.fromGuessRequestTO(guess))
						.toGuessResponseTO());
		} catch(Exception e) {
			logger.error("Error in guess: ", e);
			return ResponseEntity.badRequest().body(
					MessageGameFlowHelper.getException("making this guess", e));
		}
	}
}
