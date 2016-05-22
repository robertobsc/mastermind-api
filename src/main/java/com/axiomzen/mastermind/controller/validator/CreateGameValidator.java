package com.axiomzen.mastermind.controller.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.axiomzen.mastermind.domain.User;

@Component("createGameValidator")
public class CreateGameValidator implements Validator {

	@Override
	public boolean supports(Class<?> arg0) {
		return User.class.equals(arg0);
	}

	@Override
	public void validate(Object arg0, Errors errors) {
		User user = (User) arg0;
		
		if (user == null) {
			errors.reject("User argument was not passed. " + 
					"Please do pass the user in the following JSON format: {user: <name>}");
			
			return;
		}
		if (user.getUser() == null || user.getUser().isEmpty()) {
			errors.reject("User name not passed.");
		}
	}

}
