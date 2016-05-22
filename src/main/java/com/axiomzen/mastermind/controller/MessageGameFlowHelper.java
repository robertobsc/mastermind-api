package com.axiomzen.mastermind.controller;

import com.axiomzen.mastermind.domain.MastermindGame;
import com.axiomzen.mastermind.domain.to.GuessResponseTO;
import com.axiomzen.mastermind.domain.to.InitialGameResponseTO;
import com.axiomzen.mastermind.domain.to.MessageTO;

public class MessageGameFlowHelper {

	public static MessageTO getException(String action, Exception e) {
		return new MessageTO("Ops! Unexpected behavior while " + action + ": " + e.getMessage());
	}
	
	public static MessageTO watingForPlayers(MastermindGame game){
		return new MessageTO(
				"Waiting for other user to join the game with key " 
				+ game.getKey() + " ...\n");
	}
	
	public static MessageTO userJoinedWaitingForMore(MastermindGame game) {
		
		return new MessageTO("New user joined! Wainting for more " + 
				(game.getNumberOfUsers() - game.getUsers().size()));
	}	
	
	public static InitialGameResponseTO userJoined(MastermindGame game) {
		InitialGameResponseTO to = game.toInitialGameResponseTO();
		
		to.setMessage("Now you can start guessing at /multiplayer/guess endpoint!"
				+ " We will wait for both guesses to return an anwser.");
		
		return to;
	}
	public static MessageTO watingForGuesses(MastermindGame game){
		return new MessageTO(
				"Waiting for user(s) to guess " 
				+ game.getUserNamesNotGuessed() + " ...\n");
	}
	
	public static GuessResponseTO enfOfTurn(MastermindGame game) {
		GuessResponseTO to = game.toGuessResponseTO();
		
		to.setMessage(game.isGameSolved() ? "Seems we've got a winner!!!" : 
			"End of turn. Prepare for another rund at /multiplayer/guess endpoint!");
		
		return to;
	}
}
