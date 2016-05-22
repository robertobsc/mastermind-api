package com.axiomzen.mastermind.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	
	private String gameKey;
	private GameRule rule = null;
	private Date startDate;
	private List<ValidColors> anwser;
	private List<User> users;
	private Integer numberOfUsers;
	private List<Guess> guesses;
	private Guess currentGuess;
	private int turnNumber;
	private Map<Integer, List<Guess>> guessesInTurn;
	private boolean nextGuessNewTurn;
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
	public static MastermindGame create(User user, int numOfUsers, GameRule rule){
		MastermindGame game = new MastermindGame();
		game.turnNumber = 1;
		game.rule = rule;
		game.anwser = MastermindGame.generateAnwser();
		game.users = new ArrayList<>();
		game.users.add(user);
		game.numberOfUsers = numOfUsers;
		game.setStartDate(Calendar.getInstance().getTime());
		game.guessesInTurn = new HashMap<>();
		game.guesses = new ArrayList<>();
		
		return registry.create(game);
	}
	
	public static MastermindGame get(String key){
		return registry.get(key);
	}
	public static MastermindGame save(MastermindGame game){
		return registry.save(game);
	}
	
	public static List<String> validateGuess(Guess guess) {
		List<String> errors = Guess.validate(guess);
		
		if (! errors.isEmpty()) {
			return errors;
		}
		
		MastermindGame game = get(guess.getGameKey());
		if (game == null) {
			errors.add("The game Key passed " + guess.getGameKey() + " is invalid.");
			return errors;
		}
		
		User user = (game.numberOfUsers.equals(1)) ? 
				game.getUsers().get(0) : guess.getUser();
		if(game.hasUserPlayedInTurn(user) && !game.isNextGuessNewTurn()) {
			errors.add("User " + user.getUser() + " has already played. Wait for the next turn.");
		}
		errors.addAll(game.getRule().validate(guess, game));
		
		return errors;
	}
	
	public static MastermindGame doNewGuess(Guess guess){
		MastermindGame g = fromGuess(guess);
		g.currentGuess = guess;
		if (g.isNextGuessNewTurn()) {
			g.nextTurn();
			g.setNextGuessNewTurn(false);
		}
		
		GuessResult result = (GuessResult) g.getRule().process(guess, g);
		guess.setResult(result);
		
		if (!g.gameSolved) {
			g.gameSolved = result.getQttExactly() == ValidColors.values().length;
		}
		
		g.getGuesses().add(guess);
		g.addGuessInTurn(guess);
		
		if(g.isEndOfTurn()) {
			g.setNextGuessNewTurn(true);
		}
		return save(g);
	}
	public void nextTurn() {
		turnNumber++;
	}
	
	public boolean hasUserPlayedInTurn(User user){
		String userKey = user.getKey();
		
		return getUsersGuessedInTurn()
			.stream()
			.anyMatch(u -> userKey.equals(u.getKey()));
	}
	public List<String> getUserNamesNotGuessed() {
		Set<User> usersGuessed = getUsersGuessedInTurn();

		return 
		getUsers()
			.stream()
			.filter(u -> !usersGuessed.contains(u))
			.map(User::getUser)
			.collect(Collectors.toList());
	}
	
	public InitialGameResponseTO toInitialGameResponseTO(){
		return toInitialGameResponseTO(new InitialGameResponseTO());
	}
	
	public GuessResponseTO toGuessResponseTO() {
		GuessResponseTO to = isGameSolved() ? new GameWonTO() : new GuessResponseTO(); 
		to = (GuessResponseTO) toInitialGameResponseTO(to);
		
		to.setResult(getGuessesInTurn().get(getTurnNumber())
				.stream()
				.map(Guess::toGuessResultTO).collect(Collectors.toList()));
		
		if (isGameSolved()) {
			GameWonTO wonTo = (GameWonTO) to;
			
			Long now = Calendar.getInstance().getTimeInMillis();
			Long startTime = startDate.getTime();
			
			Calendar startDate = Calendar.getInstance();
			startDate.setTime(getStartDate());
			
			wonTo.setSolved(true);
			wonTo.setTime_taken((now - startTime)/1000.0);
			wonTo.setWinner(getNumberOfUsers().equals(1) ? 
					getUsers().get(0).getUser()
					:getCurrentGuess().getUser().getUser());
		}
		
		return to;
	}

	private Set<User> getUsersGuessedInTurn(){
		if (getNumberOfUsers().equals(1)) {
			return new HashSet<>(getUsers());
		}
		List<Guess> guesses = getGuessesInTurn().get(getTurnNumber());
		if (guesses != null) {
		return guesses
				.stream()
				.map(Guess::getUser)
				.collect(Collectors.toSet());
		}
		return new HashSet<>();
	}
	private void addGuessInTurn(Guess guess) {
		List<Guess> guesses = getGuessesInTurn().get(getTurnNumber());
		if (guesses == null) {
			guesses = new ArrayList<>();
			getGuessesInTurn().put(getTurnNumber(), guesses);
		}
		guesses.add(guess);
	}
	private InitialGameResponseTO toInitialGameResponseTO(InitialGameResponseTO to){
		
		to.setColors(Arrays.asList(ValidColors.values()));
		to.setGame_key(getGameKey());
		
		int pastResSize = 0;
		
		if (getGuesses() != null && !getGuesses().isEmpty()) {
			pastResSize = getGuesses().size() - 1;
			to.setPast_results(
					getGuesses().stream()
					.limit(pastResSize)
					.map(Guess::toGuessResultTO)
					.collect(Collectors.toList()));
		}
		to.setUsers(getUsers().stream().map(User::getUser).collect(Collectors.toList()));
		to.setSolved(isGameSolved());
		
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
	public List<User> getUsers() {
		return users;
	}
	public void setUser(List<User> users) {
		this.users = users;
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
	public int getTurnNumber() {
		return turnNumber;
	}
	public Map<Integer, List<Guess>> getGuessesInTurn() {
		return guessesInTurn;
	}
	public boolean isEndOfTurn() {
		return guessesInTurn.get(getTurnNumber()).size() == getUsers().size();
	}
	public Integer getNumberOfUsers() {
		return numberOfUsers;
	}
	public void setNumberOfUsers(Integer numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}
	public boolean isNextGuessNewTurn() {
		return nextGuessNewTurn;
	}
	public void setNextGuessNewTurn(boolean nextGuessNewTurn) {
		this.nextGuessNewTurn = nextGuessNewTurn;
	}
}
