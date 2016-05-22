package com.axiomzen.mastermind.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private static Map<String, List<ResponseBodyEmitter>> gameEmitters = new HashMap<>();
	
	List<ResponseBodyEmitter> getGameEmitters(String gameKey) {
		return gameEmitters.get(gameKey);
	}
	
	void addGameEmitter(String gameKey, ResponseBodyEmitter emitter) {
		List<ResponseBodyEmitter> list = gameEmitters.get(gameKey);
		if (list == null) {
			list = new ArrayList<>();
			gameEmitters.put(gameKey, list);
		}
		list.add(emitter);
	}
	
	void removeGameEmitters(String gameKey) {
		gameEmitters.remove(gameKey);
	}
	
	@Async
	public void emitExceptionResponse(ResponseBodyEmitter emitter, 
			String action, Exception e) throws IOException {
		emitter.send(MessageGameFlowHelper.getException(action, e));
		emitter.complete();
	}
	
	@Async
	public void emitValidationErrors(ResponseBodyEmitter emitter,
			String gameKey, String userKey, Errors errors) throws IOException {
		emitter.send(ValidationErrosBuilder.build(gameKey, userKey, errors));
		emitter.complete();
	}
	
	@Async void emitUserKey(ResponseBodyEmitter emitter, MastermindGame game, 
			String userKey) throws IOException {
		emitter.send(MessageGameFlowHelper.getUserKey(userKey));
	}
	
	@Async
	public void emitStartGameResponse(ResponseBodyEmitter emitter, 
			Errors errors, MastermindGame game) throws IOException {
		logger.debug(">> emitStartGameResponse");
		
		if (errors.hasErrors()) {
			emitValidationErrors(emitter, game.getKey(), game.getUsers().get(0).getKey(), errors);
		} else {
			emitter.send(MessageGameFlowHelper.watingForPlayers(game));
		}
		
		logger.debug("<< emitStartGameResponse");
	}
	
	@Async
	public void emitUserJoinedMessageForAll(MastermindGame game) {
		logger.debug(">> emitUserJoinedMessageForAll");
		
		getGameEmitters(game.getKey())
		.forEach(e -> {
			try {
				emitUserJoinedMessage(e, game);
			} catch (Exception e1) {
				logger.error("Unexpected error while emitting msg: ", e1);
			}
		});
		
		logger.debug("<< emitUserJoinedMessageForAll");
	}
	
	@Async
	public void emitUserJoinedMessage(ResponseBodyEmitter emitter, 
			MastermindGame game) throws IOException {
		logger.debug(">> emitUserJoinedMessage");
		
		emitter.send(MessageGameFlowHelper.userJoinedWaitingForMore(game));
		
		logger.debug("<< emitUserJoinedMessage");
	}

	@Async
	public void emitPartyCompletedMessageForAll(MastermindGame game) {
		logger.debug(">> emitPartyCompletedMessageForAll");
		
		getGameEmitters(game.getKey())
		.forEach(e -> {
			try {
				emitPartyCompletedMessage(e, game);
			} catch (Exception e1) {
				logger.error("Unexpected error while emitting msg: ", e1);
			}
		});
		removeGameEmitters(game.getGameKey());
		
		logger.debug("<< emitPartyCompletedMessageForAll");
	}
	
	@Async 
	public void emitPartyCompletedMessage(ResponseBodyEmitter emitter, MastermindGame game) throws IOException {
		
		logger.debug(">> emitPartyCompletedMessage");

		emitter.send(MessageGameFlowHelper.userJoined(game));
		emitter.complete();
		
		logger.debug("<< emitPartyCompletedMessage");
	}

	@Async
	public void emitPlayerGuessedMessageForAll(MastermindGame game) throws IOException {
		logger.debug(">> emitPlayerGuessedMessageForAll");
		
		getGameEmitters(game.getKey())
		.forEach(e -> {
			try {
				emitPlayerGuessedMessage(e, game);
			} catch (Exception e1) {
				logger.error("Unexpected error while emitting msg: ", e1);
			}
		});
		logger.debug("<< emitPlayerGuessedMessageForAll");
	}
	
	@Async
	public void emitPlayerGuessedMessage(ResponseBodyEmitter emitter, 
			MastermindGame game) throws IOException {
		logger.debug(">> emitPlayerGuessedMessage");
		
		emitter.send(MessageGameFlowHelper.watingForGuesses(game));
		logger.debug("<< emitPlayerGuessedMessage");
	}

	@Async
	public void emitEndOfTurnForAll(MastermindGame game) throws IOException {
		logger.debug(">> emitEndOfTurnForAll");
		
		getGameEmitters(game.getKey())
		.forEach(e -> {
			try {
				emitEndOfTurn(e, game);
			} catch (Exception e1) {
				logger.error("Unexpected error while emitting msg: ", e1);
			}
		});
		removeGameEmitters(game.getGameKey());
		
		logger.debug("<< emitEndOfTurnForAll");
	}
	
	@Async 
	public void emitEndOfTurn(ResponseBodyEmitter emitter, 
			MastermindGame game) throws IOException {
		
		logger.debug(">> emitEndOfTurn");

		emitter.send(MessageGameFlowHelper.enfOfTurn(game));
		emitter.complete();
		
		logger.debug("<< emitEndOfTurn");
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
