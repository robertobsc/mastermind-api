package com.axiomzen.mastermind.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.axiomzen.mastermind.domain.MastermindRule.ValidColors;
import com.axiomzen.mastermind.domain.interfaces.Game;
import com.axiomzen.mastermind.domain.interfaces.GameRule;
import com.axiomzen.mastermind.domain.registry.MastermindGameRegistry;
import com.axiomzen.mastermind.domain.to.GameWonTO;
import com.axiomzen.mastermind.domain.to.GuessResponseTO;
import com.axiomzen.mastermind.domain.to.InitialGameResponseTO;

public class MastermindGame extends Entity implements Game{
	
	private GameRule rule = null;
	private Date startDate;
	private User user;
	private String gameKey;
	private List<ValidColors> anwser;
	private List<Guess> guesses = new ArrayList<>();
	private Guess currentGuess;
	private boolean gameSolved = false;
	
	private MastermindGame(){}

	private static MastermindGameRegistry registry = new MastermindGameRegistry();
	
	@Override
	public String getKey() {
		return getGameKey();
	}
	@Override
	public void setKey(String key) {
		this.gameKey = key;
	}
	public static MastermindGame create(User user, GameRule rule){
		MastermindGame game = new MastermindGame();
		game.rule = rule;
		game.anwser = MastermindGame.generateAnwser();
		game.setUser(user);
		game.setStartDate(Calendar.getInstance().getTime());
		
		return registry.create(game);
	}
	
	public static MastermindGame get(String key){
		return registry.get(key);
	}
	
	public static List<String> validateGuess(Guess guess) {
		List<String> errors = Guess.validate(guess);
		
		if (! errors.isEmpty()) {
			return errors;
		}
		
		MastermindGame game = get(guess.getGameKey());
		if (game == null) {
			errors.add("The game Key passed " + guess.getGameKey() + " is invalid.");
		}
		errors.addAll(game.getRule().validate(guess, game));
		
		return errors;
	}
	
	public static GuessResponseTO doNewGuess(Guess guess){
		MastermindGame g = fromGuess(guess);
		g.currentGuess = guess;
		
		GuessResult result = (GuessResult) g.getRule().process(guess, g);
		guess.setResult(result);
		
		g.gameSolved = result.getQttExactly() == ValidColors.values().length;
		
		GuessResponseTO ret = g.toGuessResponseTO();
		
		g.getGuesses().add(guess);
		g.currentGuess = null;
		
		return ret;
	}
	
	private InitialGameResponseTO toInitialGameResponseTO(InitialGameResponseTO to){
		
		to.setColors(Arrays.asList(ValidColors.values()));
		to.setGame_key(getGameKey());
		to.setPast_results(
				getGuesses().stream()
				.map(Guess::toGuessResultTO)
				.collect(Collectors.toList()));
		
		to.setSolved(isGameSolved());
		
		return to;		
	}
	public InitialGameResponseTO toInitialGameResponseTO(){
		return toInitialGameResponseTO(new InitialGameResponseTO());
	}
	
	private GuessResponseTO toGuessResponseTO() {
		GuessResponseTO to = isGameSolved() ? new GameWonTO() : new GuessResponseTO(); 
		to = (GuessResponseTO) toInitialGameResponseTO(to);
		
		to.setResult(getCurrentGuess().toGuessResultTO());
		if (isGameSolved()) {
			GameWonTO wonTo = (GameWonTO) to;
			
			Long now = Calendar.getInstance().getTimeInMillis();
			Long startTime = startDate.getTime();
			
			Calendar startDate = Calendar.getInstance();
			startDate.setTime(getStartDate());
			
			wonTo.setSolved(true);
			wonTo.setTime_taken((now - startTime)/1000.0);
			wonTo.setUser(user.getUser());
		}
		
		return to;
	}

	private static MastermindGame fromGuess(Guess guess) {
		if (guess == null) {
			throw new IllegalArgumentException("Guess is invalid.");
		}
		
		MastermindGame game = get(guess.getGameKey());
		if (game == null) {
			throw new IllegalArgumentException("The game Key passed " + guess.getGameKey() + " is invalid.");
		}
		
		return game;
	}
	
	private static List<ValidColors> generateAnwser(){
		ValidColors validArray[] = ValidColors.values();
		
		List<Integer> indexList = 
				IntStream.range(0, validArray.length)
					.boxed()
					.collect(Collectors.toCollection(ArrayList::new));
		
		Collections.shuffle(indexList);
		
		return indexList.stream()
			.map(i -> validArray[i])
			.collect(Collectors.toList());
		
	}
	
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public String getGameKey() {
		return gameKey;
	}

	public void setGameKey(String gameKey) {
		this.gameKey = gameKey;
	}

	public List<Guess> getGuesses() {
		return guesses;
	}

	public GameRule getRule() {
		return rule;
	}

	public List<ValidColors> getAnwser() {
		return anwser;
	}

	public boolean isGameSolved() {
		return gameSolved;
	}

	public Guess getCurrentGuess() {
		return currentGuess;
	}
}
