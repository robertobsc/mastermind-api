package com.axiomzen.mastermind.domain.registry;

import java.util.HashMap;
import java.util.Map;

import com.axiomzen.mastermind.domain.User;

public class UserRegistry extends Registry<User>{

	private static Map<String, User> registryMap = new HashMap<>();
	
	@Override
	public Map<String, User> getRegistryMap() {
		return registryMap;
	}

}
