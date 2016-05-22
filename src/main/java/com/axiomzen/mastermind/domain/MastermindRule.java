package com.axiomzen.mastermind.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.axiomzen.mastermind.domain.interfaces.Game;
import com.axiomzen.mastermind.domain.interfaces.GameRule;
import com.axiomzen.mastermind.domain.interfaces.Play;
import com.axiomzen.mastermind.domain.interfaces.PlayResult;

@Component("mastermindRule")
public class MastermindRule implements GameRule{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final int MAX_GAME_PERIOD = 30;
	public static final int CALENDAR_PERIOD = Calendar.MINUTE;
	public static final String NAME_PERIOD = "Minutes";

	public enum ValidColors {
		R("R"), B("B"), G("G"), Y("Y"), O("O"), P("P"), C("C"), M("M");
		
		private String color;
		ValidColors(String color) {
			this.color = color;
		}
		
		@Override
		public String toString() {
			return color;
		}
		
		public static boolean isMember(String color){
			try {
				Enum.valueOf(ValidColors.class, color);
			} catch(IllegalArgumentException e) {
				return false;
			}
			return true;
		}
	};
	
	@Override
	public List<String> validate(Play play, Game game) {
		logger.debug(">> MastermindRule.validate");

		try {
			List<String> errors = new ArrayList<>();

			if(isMaxPlayTimeReached((MastermindGame)game)) {
				errors.add("You spent the maximum number of " + MAX_GAME_PERIOD + " " 
						    + NAME_PERIOD + " playing.\n" 
						    + "Please start a new game with the /new_game url!");
			}
			
			Guess guess = (Guess) play;
			if (guess == null) {
				errors.add("Received a null object!");
			} else if (guess.isValid()) {
				String colorsGuessed = guess.getGuess();
				int validLength = ValidColors.values().length;
				
				if (colorsGuessed.length() != validLength) {
					errors.add("Your guess should pass exactly " + validLength + " colors.");
				}
				char guessArray[] = colorsGuessed.toCharArray();
				for (int i = 0; i < validLength; i++) {
					char c = guessArray[i];
					if (!ValidColors.isMember(String.valueOf(c))){
						errors.add("The color " + c + " is not valid. " +
								"Valid colors are " + Arrays.toString(ValidColors.values()));
					}
				}						
				
			} else {
				errors.add("Received an invalid guess.");
			}
			
			return errors;
			
		} catch (Exception e) {
			logger.error("MastermindRule.validate - unexpected exception: ", e);
			throw e;
		} finally {
			logger.debug("<< mastermindRule.validate");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlayResult process(Play play, Game game) {
		logger.debug(">> process");
		
		Guess guess = (Guess) play;
		GuessResult result = new GuessResult();
	
		try {
			Map<ValidColors, Integer> anwserMap = 
					toValidColorMap((List<ValidColors>)game.getAnwser());
			
			char charGuesses[] = guess.getGuess().toCharArray();
			
			int qttExactly = 0;
			int qttNear = 0;
			
			for (int i = 0; i < charGuesses.length; i++) {
				Integer index = anwserMap.get(
						Enum.valueOf(ValidColors.class, String.valueOf(charGuesses[i])));
				
				if (index != null) {
					if (index == i) {
						qttExactly++;
					} else {
						qttNear++;
					}
				}
			}
			
			result.setQttExactly(qttExactly);
			result.setQttNear(qttNear);
			
		} catch(Exception e) {
			logger.error("MastermindRule.process - Unexpected error while proccessing: ", e);
			throw e;
		} finally {
			logger.debug("<< process");
		}
		return result;
	}
	
	private static boolean isMaxPlayTimeReached(MastermindGame game){
		Calendar actualTime = Calendar.getInstance();
		actualTime.add(CALENDAR_PERIOD, MAX_GAME_PERIOD * (-1));
		
		return game.getStartDate().before(actualTime.getTime());
	}
	
	private Map<ValidColors,Integer> toValidColorMap(List<ValidColors> colors) {
		Map<ValidColors, Integer> map = new HashMap<>();
		for (int i = 0; i < colors.size(); i++) {
			map.put(colors.get(i), i);
		}
		
		return map;
	}

}
