package com.axiomzen.mastermind.controller.validator;

import java.util.stream.Collectors;

import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.axiomzen.mastermind.domain.to.ValidationErrorsTO;

public class ValidationErrosBuilder {
	
	public static ValidationErrorsTO build(Errors errors) {
		return build("", "", errors);
	}

	public static ValidationErrorsTO build(String gameKey, Errors errors) {
		return build(gameKey, "", errors);
	}
	
	public static ValidationErrorsTO build(String gameKey, String userKey, Errors errors) {
		ValidationErrorsTO to = new ValidationErrorsTO();
		
		to.setErrors(
		errors.getAllErrors()
			  .stream()
			  .map(ObjectError::getDefaultMessage)
			  .collect(Collectors.toList()));
		
		to.setGameKey(gameKey);
		to.setUserKey(userKey);
		to.setMessage("There are some error(s) with your request.");
		
		return to;
		
	}
}
