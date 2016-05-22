package com.axiomzen.mastermind.utils;
import java.security.SecureRandom;
import java.math.BigInteger;

public final class SecureKeyGenerator {

	private static SecureRandom random = new SecureRandom();

	public static String generateKey() {
		return new BigInteger(100, random).toString(32);
	}
}
