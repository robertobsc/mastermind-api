package com.axiomzen.mastermind.domain;

import com.axiomzen.mastermind.domain.interfaces.PlayResult;

public class GuessResult implements PlayResult{

	int qttNear;
	int qttExactly;
	
	public int getQttNear() {
		return qttNear;
	}
	public void setQttNear(int qttNear) {
		this.qttNear = qttNear;
	}
	public int getQttExactly() {
		return qttExactly;
	}
	public void setQttExactly(int qttExactly) {
		this.qttExactly = qttExactly;
	}

}
