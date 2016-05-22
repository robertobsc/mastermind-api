package com.axiomzen.mastermind.domain.interfaces;

import java.util.List;

public interface GameRule {

	public List<String> validate(Play play, Game game);
	public PlayResult process(Play play, Game game);
}
