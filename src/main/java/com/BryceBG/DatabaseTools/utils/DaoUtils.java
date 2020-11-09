package com.BryceBG.DatabaseTools.utils;

public class DaoUtils {
	/**
	 * Helper function to verify that strings passed in are not empty null or only
	 * whitespace
	 * 
	 * @param toCheck String to check if it is "okay"
	 * @return true if it passes all the above requirements.
	 */
	public static boolean stringIsOk(String toCheck) {
		return toCheck != null && !toCheck.isBlank() && !toCheck.isEmpty();
	}

	/**
	 * Small helper function to find the smallest value in an array. Used to break
	 * ties and determine the "primary" author of a book and series
	 * 
	 * @param arrayToSearch The array to search for the smallest value in.
	 * @return the smallestValue in the array.
	 */
	private static int findSmallest(int[] arrayToSearch) {
		int curBest = Integer.MAX_VALUE;
		for (int t : arrayToSearch) {
			if (t < curBest)
				curBest = t;
		}
		return curBest;
	}

	/**
	 * Given a list of author_ids this function will determine which is the "primary_author_id" of an object in our system.
	 * @param authorIDs A list of authorIDs (we presume they are valid authorIDs and do not query the database to ensure this to reduce execution overhead.
	 * @return the authorID from the list that is the primary author.
	 */
	public static int findPrimaryAuthor(int[] authorIDs) {
		return findSmallest(authorIDs);
	}

}
