package com.axiomzen.mastermind.domain;

import com.axiomzen.mastermind.domain.registry.UserRegistry;

public class User extends Entity{
	
	private String key;
	private String user;
	private static UserRegistry registry = new UserRegistry();

	public User() {
	}
	
	public static User create(String userName){
		User user = new User();
		user.setUser(userName);
		
		return registry.create(user);
	}
	public static User save(User user) {
		return registry.save(user);
	}
	public static User get(String key){
		return registry.get(key);
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user){
		this.user = user;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}
}
