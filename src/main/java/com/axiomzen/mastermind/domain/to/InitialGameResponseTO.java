package com.axiomzen.mastermind.domain.to;

import java.util.List;

import com.axiomzen.mastermind.domain.MastermindRule.ValidColors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

//@JsonInclude(Include.NON_EMPTY)
public class InitialGameResponseTO extends MessageTO{
/*	{ 
	    "colors": ["R","B","G","Y","O","P","C","M"], 
	    "code_length": 8, 
	    "game_key": "niBpjqhujvM9NR0CQrB6e_xJXXWNNRLgfwYu8YPI3wpn4JdXs3ufRzOAv3SEC_0BNSw", 
	    "num_guesses": 0, 
	    "past_results": [], 
	    "solved": "false" 
	}*/

	private List<String> users;	
	private List<ValidColors> colors;
	private int code_length;
	private String game_key;
	private int num_guesses;
	private List<GuessResultTO> past_results;
	private boolean solved;
	
	public List<ValidColors> getColors() {
		return colors;
	}
	public void setColors(List<ValidColors> colors) {
		this.colors = colors;
	}
	public int getCode_length() {
		return colors.size();
	}
	public String getGame_key() {
		return game_key;
	}
	public void setGame_key(String game_key) {
		this.game_key = game_key;
	}
	public int getNum_guesses() {
		return (past_results.isEmpty()) ? 0 : past_results.size() + 1;
	}
	public List<GuessResultTO> getPast_results() {
		return past_results;
	}
	public void setPast_results(List<GuessResultTO> past_results) {
		this.past_results = past_results;
	}
	public boolean isSolved() {
		return solved;
	}
	public void setSolved(boolean solved) {
		this.solved = solved;
	}
	public List<String> getUsers() {
		return users;
	}
	public void setUsers(List<String> users) {
		this.users = users;
	}
}
