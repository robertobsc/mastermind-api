package com.axiomzen.mastermind.domain.to;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ValidationErrorsTO {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> errors = new ArrayList<>();

	private String errorMessage;

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	public void setErrorMessage(String msg) {
		this.errorMessage = msg;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
}
