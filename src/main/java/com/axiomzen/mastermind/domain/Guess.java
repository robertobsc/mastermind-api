package com.axiomzen.mastermind.domain;

import java.util.ArrayList;
import java.util.List;

import com.axiomzen.mastermind.domain.interfaces.Play;
import com.axiomzen.mastermind.domain.to.GuessReqTO;
import com.axiomzen.mastermind.domain.to.GuessResultTO;

public class Guess implements Play{

	private String gameKey;
	private String guess;
	private User user;
	private GuessResult result;
	
	public static List<String> validate(Guess guess) {
		List<String> errMessages = new ArrayList<>();
		
		if (guess == null ||
			guess.getGameKey() == null || guess.getGameKey().isEmpty()) {
				errMessages.add("This guess is invalid. Please pass a valid guess");
		}
		
		return errMessages;
	}
	
	public boolean isValid(){
		return validate(this).isEmpty();
	}
	
	public static Guess fromGuessRequestTO(GuessReqTO reqTO) {
		Guess guess = new Guess();
		guess.setGuess(reqTO.getCode());
		guess.setGameKey(reqTO.getGame_key());
		guess.setUser(User.get(reqTO.getUser_key()));
		
		return guess;
	}
	
	public GuessResultTO toGuessResultTO(){
		GuessResultTO to = new GuessResultTO();
		to.setExactly(result.getQttExactly());
		to.setNear(result.getQttNear());
		to.setGuess(getGuess());
		if (getUser() != null){
			to.setUser(getUser().getUser());
		}
		
		return to;
	}
	public String getGameKey() {
		return gameKey;
	}
	public void setGameKey(String gameKey) {
		this.gameKey = gameKey;
	}
	public String getGuess() {
		return guess;
	}
	public void setGuess(String guess) {
		this.guess = guess;
	}
	public GuessResult getResult() {
		return result;
	}
	public void setResult(GuessResult result) {
		this.result = result;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
