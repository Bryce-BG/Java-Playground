package com.BryceBG.DatabaseTools.utils;

public class DBUtils {
	/**
	 * Helper function to verify that strings passed in are not empty null or only whitespace
	 * @param toCheck String to check if it is "okay"
	 * @return true if it passes all the above requirements.
	 */
	public static boolean stringIsOk(String toCheck) {
		return toCheck!=null && !toCheck.isBlank() && !toCheck.isEmpty();
	}
}
