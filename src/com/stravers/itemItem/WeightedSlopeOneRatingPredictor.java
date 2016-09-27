package com.stravers.itemItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import com.stravers.userPreferences.UserPreferences;

public class WeightedSlopeOneRatingPredictor {
	
	
	private static final int USER_DEVIATION_CARDINALITY_SUBTOTAL 	= 0;
	private static final int CARDINALITY_SUBTOTAL 					= 1;
	private static final String DIVIDE_BY_ZERO						= "Attempted to divide by zero!";
	
	private UserPreferences allUserPreferences;
	public WeightedSlopeOneDeviationTableCalculator weightedSlopeOneDeviatonTableCalculator;
	
	public WeightedSlopeOneRatingPredictor (UserPreferences allUserPreferences) {
		this.allUserPreferences = allUserPreferences;
		this.weightedSlopeOneDeviatonTableCalculator = new WeightedSlopeOneDeviationTableCalculator();
		weightedSlopeOneDeviatonTableCalculator.createDeviationTable(allUserPreferences);
	} 
	
		
	/**
	 * predicts the user's rating for a target item based on an item-item comparison using a weighted slope. Checks for isNaN
	 * deviation scores, which occur if no users rated both the current and compared item during deviation table generation.
	 * @param userID (int): the id of the target user
	 * @param targetItemID (int): the id of the item to predict the rating for
	 * @return predictedRatingForTargetItem (double): the predicted rating for the specified item
	 */
		public double predictRating(int userID, int targetItemID) {
		double userRatingsPlusDeviationsTimesCardinality = 0;
		int totalRatingsCardinality = 0; 
		HashMap<Integer, Double> currentUsersRatings = allUserPreferences.ratingsCollection.get(userID);
		Iterator<Entry<Integer, Double>> usersItemRatingsIterator = currentUsersRatings.entrySet().iterator(); // loop through all existing items
	    while (usersItemRatingsIterator.hasNext()) {
	    	Map.Entry<Integer, Double> usersCurrentItemAndRating = (Map.Entry<Integer, Double>) usersItemRatingsIterator.next();
	    	Vector<Number> calculationSubTotals = calculateCurrentItemRatingsDeviationCardinalityScoresAndCardinalitySubTotals(usersCurrentItemAndRating, targetItemID);
	    	double currentCalculationSubTotal = (double) calculationSubTotals.get(USER_DEVIATION_CARDINALITY_SUBTOTAL);
	    	if(!Double.isNaN(currentCalculationSubTotal)){
	    		userRatingsPlusDeviationsTimesCardinality += currentCalculationSubTotal;
		    	totalRatingsCardinality += (int) calculationSubTotals.get(CARDINALITY_SUBTOTAL);
	    	}
	    }
	    if(totalRatingsCardinality > 0) {
	    	double predictedRatingForTargetItem = (userRatingsPlusDeviationsTimesCardinality / totalRatingsCardinality);
	    	return predictedRatingForTargetItem;
	    }
	    else {
	    	Utilities.printMessage(DIVIDE_BY_ZERO);
	    	return (Double) null;
	    }
	}
	
	
	/**
	 * Calculates the subtotals used for predicting the rating per item the user rated. Takes the user's rating per item,
	 * adds the pre-calculated deviationscore for that item and multiplies the result by the number of ratings the deviation
	 * score is based on.
	 * @param usersCurrentItemAndRating (Map.Entry<Integer, Double>): The current itemID and the rating the user assigned it
	 * @param targetItemID (int): the id of the item to predict the user's rating for
	 * @return subTotalsRatingsDeviationCardinalityScoresAndCardinality (Vector<Number>): the subtotal of the current item
	 * 		for (userRating+deviationScore) * cardinality and cardinality
	 */
	private Vector<Number> calculateCurrentItemRatingsDeviationCardinalityScoresAndCardinalitySubTotals(
			Map.Entry<Integer, Double> usersCurrentItemAndRating, int targetItemID) {
		Vector<Number> subTotalsRatingsDeviationCardinalityScoresAndCardinality = new Vector<Number>();
		int currentRatedItemID = (int) usersCurrentItemAndRating.getKey();
		int combinedKey = Utilities.generateCombinedKey(targetItemID, currentRatedItemID);
    	Vector<Number> itemDeviationScoreAndN = weightedSlopeOneDeviatonTableCalculator.completeDeviationTable.get(combinedKey);    	
    	double usersRatingForCurrentItem = (Double) usersCurrentItemAndRating.getValue();
    	double currentItemDeviationScore = (Double) itemDeviationScoreAndN.get(Utilities.DEVIATION_SCORE_IN_TABLE);
    	int currentItemDeviationScoreNumberOfRatings = (int) itemDeviationScoreAndN.get(Utilities.RATING_N_IN_TABLE);
    	Double ratingsDeviationsCardinalitySubTotal = ((usersRatingForCurrentItem + currentItemDeviationScore) * currentItemDeviationScoreNumberOfRatings);
    	subTotalsRatingsDeviationCardinalityScoresAndCardinality.add(ratingsDeviationsCardinalitySubTotal);
    	subTotalsRatingsDeviationCardinalityScoresAndCardinality.add(currentItemDeviationScoreNumberOfRatings);
    	return subTotalsRatingsDeviationCardinalityScoresAndCardinality;
	}
	
	
	/**
	 * Predicts the top K recommended items for the target user.
	 * @param userID
	 * @param k
	 */
	public void predictTopKRecommendedItems(int userID, int k) { // used K instead of N for consistency with my userItem implementation
		/**
		 * predict for all items here. Maybe dont bother predicting both XY and YX, that would decrease proc times.
		 * */
		TreeMap<Double, Integer> itemsSortedByPredictedRating = new TreeMap<Double, Integer>();
		HashMap<Integer, Double> currentUsersRatings = allUserPreferences.ratingsCollection.get(userID);
		for(Integer itemID : allUserPreferences.allItemIDs){
			if(!currentUsersRatings.containsKey(itemID)){
				Double predictedRating = predictRating(userID, itemID);
				itemsSortedByPredictedRating.put(predictedRating, itemID);
			}
		}
		printTopKItems(itemsSortedByPredictedRating, k);
	}
	
	
	private void printTopKItems(TreeMap<Double, Integer> itemsSortedByPredictedRating, int k) {
		int numberOfItemsDisplayed = 0;
		for(Double key : itemsSortedByPredictedRating.descendingKeySet()) {
			numberOfItemsDisplayed++;
			printItemAndRating(key, itemsSortedByPredictedRating.get(key));
			if(numberOfItemsDisplayed >= k) {
				return;
			}
		}
	}
	
	
	private void printItemAndRating(double rating, int itemID) {
		Utilities.printMessage(itemID + ": " + rating);
	}
}
