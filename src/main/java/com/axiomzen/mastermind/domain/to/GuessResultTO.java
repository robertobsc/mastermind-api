package com.axiomzen.mastermind.domain.to;

public class GuessResultTO {

	private int exactly;
	private String guess;
	private int near;
	
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
	
}
