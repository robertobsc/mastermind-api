package com.axiomzen.mastermind.controller.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.axiomzen.mastermind.domain.Guess;
import com.axiomzen.mastermind.domain.MastermindGame;
import com.axiomzen.mastermind.domain.to.GuessReqTO;

@Component("guessRequestValidator")
public class GuessRequestValidator implements Validator {

	@Override
	public boolean supports(Class<?> arg0) {
		return GuessReqTO.class.equals(arg0);
	}

	@Override
	public void validate(Object arg0, Errors errors) {
		GuessReqTO to = (GuessReqTO)arg0;
		
		if (to.getGame_key() == null || to.getGame_key().isEmpty()) {
			errors.reject("Empy game key");
		}
		if (to.getCode() == null || to.getCode().isEmpty()) {
			errors.reject("Invalid guess.");
		}
		if (errors.hasErrors()) {
			return;
		}
		MastermindGame.validateGuess(
				Guess.fromGuessRequestTO(to)).forEach(errors::reject);
	}

}
