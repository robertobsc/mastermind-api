package com.axiomzen.mastermind.controller.validator;

import org.springframework.validation.Errors;

import com.axiomzen.mastermind.domain.MastermindGame;

public class MultiPlayerValidator {

	public static boolean checkPartyFull(String gameKey, Errors errors) {
		MastermindGame game = MastermindGame.get(gameKey);
		if (game == null) return true;
		
		if (game.getUsers().size() == game.getNumberOfUsers()) {
			errors.reject("Number of users exceeded for this game. Please choose another one.");
			return true;
		}
		
		return false;
	}
}
