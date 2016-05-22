package com.axiomzen.mastermind.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.axiomzen.mastermind.controller.validator.ValidationErrosBuilder;
import com.axiomzen.mastermind.domain.MastermindGame;

@Component
public class ResponseEmitterHelper {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Async
	public Void emitStartGameResponse(ResponseBodyEmitter emitter, 
			Errors errors, MastermindGame game) throws IOException {
		logger.debug(">> emitStartGameResponse");
		
		if (errors.hasErrors()) {
			emitter.send(ValidationErrosBuilder.build(errors));
			emitter.complete();
		} else {
			emitter.send(game.toInitialGameResponseTO());
		}
		logger.debug("<< emitStartGameResponse");
		return null;
	}
	
	@Async
	public Void asyncResponse(ResponseBodyEmitter emitter, String gameKey){
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
}
