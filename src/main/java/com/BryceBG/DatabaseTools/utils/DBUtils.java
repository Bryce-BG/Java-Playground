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
	
	
	/**
	 * Small helper function to find the smallest value in an array.
	 * Used to break ties and determine the "primary" author of a book/
	 * @param arrayToSearch The array to search for the smallest value in.
	 * @return the smallestValue in the array.
	 */
	public static int findSmallest(int[] arrayToSearch) {
		int curBest = Integer.MAX_VALUE;
		for(int t: arrayToSearch) {
			if(t<curBest)
				curBest=t;
		}
		return curBest;
	}
}
