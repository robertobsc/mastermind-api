package com.axiomzen.mastermind.domain.to;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class GuessResponseTO extends InitialGameResponseTO{
/*	{ 
	    "code_length": 8, 
	    "colors": ["R","B","G","Y","O","P","C","M"], 
	    "game_key": "niBpjqhujvM9NR0CQrB6e_xJXXWNNRLgfwYu8YPI3wpn4JdXs3ufRzOAv3SEC_0BNSw", 
	    "guess": "RRBPPCBC", 
	    "num_guesses": 1, 
	    "past_results": [ 
	        { 
	            "exact": 1, 
	            "guess": "RRBPPCBC", 
	            "near": 5 
	        } 
	    ], 
	    "result": { 
	        "exact": 1, 
	        "near": 5 
	    }, 
	    "solved": "false" 
	}*/
	
	private String guess;
	private GuessResultTO result;
	public String getGuess() {
		return guess;
	}
	public void setGuess(String guess) {
		this.guess = guess;
	}
	public GuessResultTO getResult() {
		return result;
	}
	public void setResult(GuessResultTO result) {
		this.result = result;
	}
	
}
