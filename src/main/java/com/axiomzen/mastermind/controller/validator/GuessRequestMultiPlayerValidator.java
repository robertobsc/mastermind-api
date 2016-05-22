package com.axiomzen.mastermind.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.axiomzen.mastermind.domain.to.GuessReqTO;

@Component("guessRequestMultiPlayerValidator")
public class GuessRequestMultiPlayerValidator implements Validator {
	
	@Autowired
	Validator guessRequestValidator;

	@Override
	public boolean supports(Class<?> arg0) {
		return GuessReqTO.class.equals(arg0);
	}

	@Override
	public void validate(Object target, Errors errors) {
		GuessReqTO to = (GuessReqTO)target;
		
		if (to.getUser_key() == null || to.getUser_key().isEmpty()) {
			errors.reject("Invalid user key. Please inform a valid user key!");
		}
		guessRequestValidator.validate(target, errors);
	}

}
