package com.axiomzen.mastermind.domain.to;

public class GuessReqTO {

	private String code;
	private String game_key;
	private String user_key;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getGame_key() {
		return game_key;
	}
	public void setGame_key(String game_key) {
		this.game_key = game_key;
	}
	public String getUser_key() {
		return user_key;
	}
	public void setUser_key(String user_key) {
		this.user_key = user_key;
	}
}
