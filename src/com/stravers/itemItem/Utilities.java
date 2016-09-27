package com.stravers.itemItem;

public class Utilities {

	protected static final int DEVIATION_SCORE_IN_TABLE 				= 0;
	protected static final int RATING_N_IN_TABLE 						= 1;
	
	/**
	 * generates a combined key from two ints (for use in my deviation HashMap). generateCombinedKey(1,25)
	 * would generate 125 as a result.
	 * @param currentItemID (int): part one of the ints to concatenate 
	 * @param targetItemID (int): part two of the ints to concatenate
	 * @return combinedKey (int): the combined key 
	 * this should really have been in a utilities package.
	 */
	public static int generateCombinedKey(int currentItemID, int targetItemID) {
		Integer combinedKey = Integer.valueOf(String.valueOf(currentItemID) + String.valueOf(targetItemID));
		return combinedKey;
	}
	
	
	public static void printMessage(String message) {
		System.out.println(message);
	}
	
}
