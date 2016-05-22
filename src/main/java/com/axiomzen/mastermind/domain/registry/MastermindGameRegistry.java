package com.axiomzen.mastermind.domain.registry;

import java.util.HashMap;
import java.util.Map;

import com.axiomzen.mastermind.domain.MastermindGame;

public class MastermindGameRegistry extends Registry<MastermindGame>{

	private static Map<String, MastermindGame> registryMap = new HashMap<>();

	@Override
	public Map<String, MastermindGame> getRegistryMap() {
		return registryMap;
	}
}
