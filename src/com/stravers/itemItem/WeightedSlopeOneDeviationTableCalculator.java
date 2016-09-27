package com.stravers.itemItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.stravers.userPreferences.UserPreferences;

public class WeightedSlopeOneDeviationTableCalculator {
	
	private static final int COMBINED_KEY								= 0;
	private static final int DEVIATION_SCORE 							= 1;
	private static final int NUMBER_OF_RATINGS 							= 2;
	private static final int COMPARED_ITEM_EQUALS_TARGET_ITEM 			= 0;
	private static final double DEVIATIONXiXi_EQUALS_ZERO				= 0.0;
	private static final String FINISHED_CALCULATING_DEVIATION_TABLE 	= "Finished generating deviation table.";

	
	protected HashMap<Integer, Vector<Number>> completeDeviationTable;
	private Set<Integer> completeListOfRatedItems;
	
	public WeightedSlopeOneDeviationTableCalculator() {
		this.completeDeviationTable = new HashMap<Integer, Vector<Number>>();
	}
	
	
	/**
	 * extracts deviation scores for each unique item rated by users.
	 * @param allUserPreferences (UserPreferences): object containing all user's ratings for items.
	 * @return deviationTable (HashMap<Integer, Vector<Number>): a hashmap containing the deviationscore and the
	 * 		number of ratings it's based on in a vector, accessed through the hashmap by a combined key consisting of
	 * 		current item id + target item id. So a comparison between item 101 and 103: 
	 * 		HashMap(101103: Vector[deviationscore, numberOfComparedRatings]).
	 */
	public void createDeviationTable(UserPreferences allUserPreferences) {	
		this.completeListOfRatedItems = allUserPreferences.allItemIDs;
		Iterator<Integer> currentItemIterator = allUserPreferences.allItemIDs.iterator(); // loop through all existing item ids
	    while (currentItemIterator.hasNext()) {
	        int currentItemID = currentItemIterator.next(); 
	        
	        Iterator<Integer> comparedItemsIterator = allUserPreferences.allItemIDs.iterator(); // loop through all existing items
		    while (comparedItemsIterator.hasNext()) {
		    	int comparedItemID = comparedItemsIterator.next();
		    	calculateDeviationScoresIfNotAlreadyCalculated(comparedItemID, currentItemID, allUserPreferences);
		    }
	    }
	    Utilities.printMessage(FINISHED_CALCULATING_DEVIATION_TABLE);
	}

	
	/**
	 * checks if the deviationscore hasn't already been calculated for the opposite combined key (when calculating 103-101,
	 * 		check if 101-103 already exists). If yes -> use the existing values *-1, if no -> calculate deviation score
	 * @param comparedItemID (int): the ID of the item the current item is being compared to
	 * @param currentItemID (int): the id of the current item
	 * @param allUserPreferences (UserPreferences): object containing all user's ratings of items.
	 */
	private void calculateDeviationScoresIfNotAlreadyCalculated(int comparedItemID, int currentItemID, 
			UserPreferences allUserPreferences) {
        int combinedKeyReversedOrder = Utilities.generateCombinedKey(comparedItemID, currentItemID);
        if(completeDeviationTable.containsKey(combinedKeyReversedOrder)) {
        	int combinedKey = Utilities.generateCombinedKey(currentItemID, comparedItemID);
        	Vector<Number> currentDeviationScoreAndN = completeDeviationTable.get(combinedKeyReversedOrder);
        	int currentN = (int) currentDeviationScoreAndN.get(1);
        	double currentDevScore =  Double.valueOf((Double) currentDeviationScoreAndN.get(0));
        	double reversedDevScorePrimitive = currentDevScore * -1;
        	Double reversedDevScore = new Double(reversedDevScorePrimitive);
        	Vector<Number> reversedDeviationScoreAndN = new Vector<Number>();
        	reversedDeviationScoreAndN.add(reversedDevScore);
        	reversedDeviationScoreAndN.add(currentN);
        	completeDeviationTable.put(combinedKey, reversedDeviationScoreAndN);
        }
        else {
        	Vector combinedKeyDeviationScoresN = calculateDeviationScoreForCurrentAndComparedItems(currentItemID, comparedItemID, allUserPreferences.ratingsCollection);
	        int combinedKey = (int) combinedKeyDeviationScoresN.get(COMBINED_KEY);
	        double deviationScore = (double) combinedKeyDeviationScoresN.get(DEVIATION_SCORE);
	        int numberOfRatings = (int) combinedKeyDeviationScoresN.get(NUMBER_OF_RATINGS);
	        Vector<Number> deviationScoreNumberOfRatings = new Vector<Number>();
	        deviationScoreNumberOfRatings.add(deviationScore);
	        deviationScoreNumberOfRatings.add(numberOfRatings);	
	        completeDeviationTable.put(combinedKey, deviationScoreNumberOfRatings);
        }
	}
  

	/**
	 * checks if the current item != compared item, extracts the item/comparedItem deviation score from all users' ratings
	 * by calling extractDeviationScoreFromAllRatings();
	 *@param currentItemID (int): the id of the current item
	 * @param comparedItemID (int): the id of the item the current item is being compared with for this calculation
	 * @param allUserPreferences (HashMap<Integer, HashMap<Integer, Double>>): nested hashmap containing (userIDs: (itemID: rating))
	 * @return deviationBetweenCurrentItemAndComparedItem (Vector<Number>): [combinedKey currentItemID+comparedItemID,
	 * 		calculated deviation score, number of comparisons the deviation is based on]
	 */
	private Vector<Number> calculateDeviationScoreForCurrentAndComparedItems(int currentItemID, int comparedItemID, 
			HashMap<Integer, HashMap<Integer, Double>> allUserPreferences) {
		Vector<Number> deviationBetweenCurrentItemAndComparedItem = new Vector<Number>();
		if(currentItemID == comparedItemID) {
			int combinedKey = Utilities.generateCombinedKey(currentItemID, comparedItemID);
			deviationBetweenCurrentItemAndComparedItem.add(combinedKey);
			deviationBetweenCurrentItemAndComparedItem.add(DEVIATIONXiXi_EQUALS_ZERO);
			deviationBetweenCurrentItemAndComparedItem.add(COMPARED_ITEM_EQUALS_TARGET_ITEM);
		}
		else{
			deviationBetweenCurrentItemAndComparedItem = extractDeviationScoreFromAllRatings(currentItemID, comparedItemID,
					allUserPreferences, deviationBetweenCurrentItemAndComparedItem);
		}
		return deviationBetweenCurrentItemAndComparedItem;
	}
	
	
	/**
	 * Loops through all user ratings. calculates deviation score if current and target item are found.
	 * @param currentItemID (int): the id of the current item
	 * @param comparedItemID (int): the id of the item the current item is being compared with for this calculation
	 * @param allUserPreferences (HashMap<Integer, HashMap<Integer, Double>>): nested hashmap containing (userIDs: (itemID: rating))
	 * @param deviationBetweenCurrentItemAndComparedItem (Vector<Number>): empty vector to hold the results
	 * @return deviationBetweenCurrentItemAndComparedItem (Vector<Number>): [combinedKey currentItemID+comparedItemID,
	 * 		calculated deviation score, number of comparisons the deviation is based on]
	 */
	private Vector<Number> extractDeviationScoreFromAllRatings(int currentItemID, int comparedItemID, 
			HashMap<Integer, HashMap<Integer, Double>> allUserPreferences, 
			Vector<Number> deviationBetweenCurrentItemAndComparedItem) {
		int numberOfComparisonsMade = 0;
		double totalRatingDifference = 0;
		Iterator<?> allRatingsIterator = allUserPreferences.entrySet().iterator(); // loop through all ratedmovie ids
	    while (allRatingsIterator.hasNext()) {
	        Map.Entry currentUsersRatingsAndItems = (Map.Entry)allRatingsIterator.next(); 
	        HashMap<Integer, Double> currentUsersItemRatings = (HashMap<Integer, Double>) currentUsersRatingsAndItems.getValue();
	        if(mapContainsCurrentAndTargetItem(currentUsersItemRatings, currentItemID, comparedItemID)) {
	        	numberOfComparisonsMade += 1;
	        	double currentItemRating = currentUsersItemRatings.get(currentItemID);
	        	double comparedItemRating = currentUsersItemRatings.get(comparedItemID);
	        	totalRatingDifference += (currentItemRating - comparedItemRating);
	        }
	    }
	    double deviationBetweenCurrentAndComparedItem = (totalRatingDifference / numberOfComparisonsMade);
	    deviationBetweenCurrentItemAndComparedItem.add(Utilities.generateCombinedKey(currentItemID, comparedItemID));
	    deviationBetweenCurrentItemAndComparedItem.add(deviationBetweenCurrentAndComparedItem);
	    deviationBetweenCurrentItemAndComparedItem.add(numberOfComparisonsMade);
	    
	    return deviationBetweenCurrentItemAndComparedItem;
	}
	
	
	
	/**
	 * Loops over all rated items, replaces ratedITemID - updatedItemID && updatedItemID - ratedITemID with updated
	 * 		deviation scores and N.
	 * @param userID (int): the id of the current user
	 * @param updatedItemID (int): the item id of the added rating
	 * @param rating (double): the rating the user gave for an item
	 */
	public void insertIntoDeviationTable(int userID, int updatedItemID, double rating) {
		for (int currentItemID : completeListOfRatedItems) {
			if(currentItemID != updatedItemID) {
				int currentItemIDUpdatedItemID = Utilities.generateCombinedKey(currentItemID, updatedItemID);
				int updatedItemIDCurrentItemID = Utilities.generateCombinedKey(updatedItemID, currentItemID);
				Vector<Number> currentItemUpdatedItemDeviationScoreN = completeDeviationTable.get(currentItemIDUpdatedItemID);
				int currentN = (int) currentItemUpdatedItemDeviationScoreN.get(Utilities.RATING_N_IN_TABLE);				
				double currentDeviationScore = (double) currentItemUpdatedItemDeviationScoreN.get(Utilities.DEVIATION_SCORE_IN_TABLE);
				int updatedN =  (currentN + 1);
				double updatedDeviationScore = (((currentDeviationScore * currentN) + rating) / updatedN);
				Vector<Number> updatedDeviationScoresAndN = new Vector<Number>();
				updatedDeviationScoresAndN.add(updatedDeviationScore);
				updatedDeviationScoresAndN.add(updatedN);
				completeDeviationTable.put(currentItemIDUpdatedItemID, updatedDeviationScoresAndN);
				completeDeviationTable.put(updatedItemIDCurrentItemID, updatedDeviationScoresAndN);
			}
		}	
	}
	
	
	private boolean mapContainsCurrentAndTargetItem(HashMap<Integer, Double> ratedItems, int currentItem, int targetItem) {
		if(ratedItems.containsKey(currentItem)) {
			if(ratedItems.containsKey(targetItem)) {
				return true;
			}
		}
		return false;
	}
}
