package com.axiomzen.mastermind.domain.to;

public class GameWonTO extends GuessResponseTO {
/*	{ 
	    "code_length": 8, 
	    "colors": ["R","B","G","Y","O","P","C","M"], 
	    "further_instructions": "Solve the challenge to see this!", 
	    "game_key": "jwrcZhiOn9Un6hBm0HnJqol8xpAGjznpGJ5A78EMqoxj-nG5vMouEJN58-l-CU0wP4M", 
	    "guess": "RBPOCBCB", 
	    "num_guesses": 2, 
	    "past_results": [ 
	        { 
	            "exact": 1, 
	            "guess": "RRBPPCBC", 
	            "near": 5 
	        }, 
	        { 
	            "exact": 8, 
	            "guess": "RBPOCBCB", 
	            "near": 0 
	        } 
	    ], 
	    "result": "You win!", 
	    "solved": "true", 
	    "time_taken": 64.75358, 
	    "user": "Alex Zimmerman" 
	}*/
	
	private Double time_taken;
	private String user;
	
	public Double getTime_taken() {
		return time_taken;
	}
	public void setTime_taken(Double time_taken) {
		this.time_taken = time_taken;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
}
