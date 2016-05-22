package com.axiomzen.mastermind.domain.to;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class GuessResultTO {

	private int exactly;
	private String guess;
	private int near;
	private String user;
	
	public int getExactly() {
		return exactly;
	}
	public void setExactly(int exactly) {
		this.exactly = exactly;
	}
	public String getGuess() {
		return guess;
	}
	public void setGuess(String guess) {
		this.guess = guess;
	}
	public int getNear() {
		return near;
	}
	public void setNear(int near) {
		this.near = near;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
}
