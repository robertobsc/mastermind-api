package com.axiomzen.mastermind.domain;

public class GameUserKey {
	private String gameKey;
	private String userKey;
	
	private GameUserKey(){}
	
	public GameUserKey(String gameKey, String userKey) {
		this.gameKey = gameKey;
		this.userKey = userKey;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameKey == null) ? 0 : gameKey.hashCode());
		result = prime * result + ((userKey == null) ? 0 : userKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameUserKey other = (GameUserKey) obj;
		if (gameKey == null) {
			if (other.gameKey != null)
				return false;
		} else if (!gameKey.equals(other.gameKey))
			return false;
		if (userKey == null) {
			if (other.userKey != null)
				return false;
		} else if (!userKey.equals(other.userKey))
			return false;
		return true;
	}
}
