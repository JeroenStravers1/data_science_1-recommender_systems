package com.stravers.userItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.stravers.userPreferences.UserPreferences;

public class UserRatingPredictor {

	private static final int MINIMUM_RATINGS = 3;
	
	private NearestNeighbourExtractor nearestNeighbourExtractor;
	
	
	public UserRatingPredictor(int targettedUser, int targettedItem, String method, int n, 
			UserPreferences userPreferences) {
		nearestNeighbourExtractor = new NearestNeighbourExtractor();
		nearestNeighbourExtractor.extractNearestNeighbours(targettedUser, targettedItem, method, n, userPreferences);
	}
	
	
	/**
	 * Predicts the rating a specified user will give to a product based on the ratings of 
	 * the top n nearest neighbours of that user.
	 * @param productIDToPredictRatingFor (int): the id of the item to predict the rating for
	 * @param similarityScores (LinkedHashMap<Integer, Double>): list of userID-similarity rating
	 * @param comparedUsersRatedMovies (HashMap<Integer, HashMap<Integer, Double>>): list of rated movies per user
	 * @return predictedRating (double): the predicted rating for user n for item productIDToPredictRatingFor
	 */
	public double predictRatingForSpecificItem(int productIDToPredictRatingFor) {
		double cumulativeSimilarityScore = 0.0;
		double cumulativeSimilarityRatingScore = 0.0;
		Iterator<?> similarUserIterator = nearestNeighbourExtractor.nMostSimilarUsersAscending.entrySet().iterator();
	    while (similarUserIterator.hasNext()) {
	        Map.Entry<Integer, Double> similarUser = (Map.Entry<Integer, Double>) similarUserIterator.next();
	        int currentUserID = similarUser.getKey(); // current user id
	        HashMap<Integer, Double> currentUsersRatedMovies = nearestNeighbourExtractor.mostSimilarUsersRatings.get(currentUserID); // getUID rated movies
	        if(currentUsersRatedMovies.containsKey(productIDToPredictRatingFor)) {
	        	Double userSimilarity = similarUser.getValue();
	        	Double userRating = currentUsersRatedMovies.get(productIDToPredictRatingFor);
	        	cumulativeSimilarityScore += userSimilarity;
	        	cumulativeSimilarityRatingScore += (userSimilarity * userRating);
	        }
	    }
	    double predictedRating = cumulativeSimilarityRatingScore / cumulativeSimilarityScore; 
		return predictedRating;
	}
	
	
	public void predictTopKRecommendedItems(int k) {
		LinkedHashMap<Integer, Double> unsortedPredictedRatedItems = new LinkedHashMap<Integer, Double>(k);
		HashMap<Integer, Integer> numberOfTargetUserUnratedItemsRatedByOtherUsers = new HashMap<Integer, Integer>();
		
		numberOfTargetUserUnratedItemsRatedByOtherUsers = extractItemsNotRatedByTargetUser();
		unsortedPredictedRatedItems = predictRatingForMultipleItems(numberOfTargetUserUnratedItemsRatedByOtherUsers);
		List<Entry<Integer, Double>> sortedEntries = new ArrayList<Entry<Integer, Double>>();
		sortedEntries = entriesSortedByValues(unsortedPredictedRatedItems);		
		printTopKItems(sortedEntries, k);
	}
	
	
	/**
	 * checks if nearest neighbours rated an item the target user did not rate. If yes, stores the item id
	 * and number of times others rated it in a HashMap<int, int>
	 * @return numberOfTargetUserUnratedItemsRatedByOtherUsers
	 */
	private HashMap<Integer, Integer> extractItemsNotRatedByTargetUser() {
		HashMap<Integer, Integer> numberOfTargetUserUnratedItemsRatedByOtherUsers = new HashMap<Integer, Integer>();
		Iterator<?> similarUsersIterator = nearestNeighbourExtractor.nMostSimilarUsersAscending.entrySet().iterator();
		while (similarUsersIterator.hasNext()) {
	        Map.Entry<Integer, HashMap> currentUser = (Entry<Integer, HashMap>) similarUsersIterator.next();
	        int currentUserID = currentUser.getKey();
	        HashMap<Integer, Double> currentUsersItems = nearestNeighbourExtractor.mostSimilarUsersRatings.get(currentUserID);
	        Iterator<?> usersItemsIterator = currentUsersItems.entrySet().iterator();
			while (usersItemsIterator.hasNext()) {
		        Map.Entry<Integer, Double> currentItem = (Entry<Integer, Double>) usersItemsIterator.next();
		        int currentItemID = currentItem.getKey();
		        if(nearestNeighbourExtractor.targetUsersRatings.containsKey(currentItemID) == false) {
		        	if(numberOfTargetUserUnratedItemsRatedByOtherUsers.containsKey(currentItemID)) {
		        		int updatedNumberOfTimesRated = (1 + numberOfTargetUserUnratedItemsRatedByOtherUsers.get(currentItemID));
		        		numberOfTargetUserUnratedItemsRatedByOtherUsers.put(currentItemID, updatedNumberOfTimesRated);
		        	}     	
		        	else {
		        		numberOfTargetUserUnratedItemsRatedByOtherUsers.put(currentItemID, 1);
		        	}
		        }	 
			}
		}
		return numberOfTargetUserUnratedItemsRatedByOtherUsers;
	}
	
	
/**
 * predicts the target user's rating for each item he did not rate that his N nearest neghbours did rate
 * @param numberOfTargetUserUnratedItemsRatedByOtherUsers (HashMap<Int, Int>): list of movieIds and number of occurences
 * @return unsortedItemsPredictedRatings(LinkedHashMap<Integer, Double>): list of movieIds and predicted ratings
 */
	private LinkedHashMap<Integer, Double> predictRatingForMultipleItems(HashMap<Integer, Integer> numberOfTargetUserUnratedItemsRatedByOtherUsers) {
		LinkedHashMap<Integer, Double> unsortedItemsPredictedRatings = new LinkedHashMap<Integer, Double>();
		Iterator<?> itemsToPredictRatingForIterator = numberOfTargetUserUnratedItemsRatedByOtherUsers.entrySet().iterator();
	    while (itemsToPredictRatingForIterator.hasNext()) {
	        Map.Entry<Integer, Double> currentItem = (Map.Entry<Integer, Double>) itemsToPredictRatingForIterator.next();
	        int currentItemID = currentItem.getKey();
	        int numberOfRatings = numberOfTargetUserUnratedItemsRatedByOtherUsers.get(currentItemID);
	        if(numberOfRatings >= MINIMUM_RATINGS) {
	        	double predictedRating = predictRatingForSpecificItem(currentItemID);
	        	unsortedItemsPredictedRatings.put(currentItemID, predictedRating);
	        }
	    }
		return unsortedItemsPredictedRatings;
	}




	// sorting linked hashmaps proved quite a challenge for me. I eventually found this method on http://stackoverflow.com/questions/11647889/sorting-the-mapkey-value-in-descending-order-based-on-the-value
	// I would love to say that I have written something like this myself, but this is currently beyond my ability. The positive side to this is
	// that I now have an example (and motivation!) to figure this out, and to allow me to write functions like these in the future!
	static <K,V extends Comparable<? super V>> 
	List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
	
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
	
		Collections.sort(sortedEntries, 
		new Comparator<Entry<K,V>>() {
		    @Override
		    public int compare(Entry<K,V> e1, Entry<K,V> e2) {
		        return e2.getValue().compareTo(e1.getValue());
		    }
		});
		return sortedEntries;
	}
	
	
	private void printTopKItems(List<Entry<Integer, Double>> sortedEntries, int k) {
		int safeK = k;
		if (k > sortedEntries.size()) {
			safeK = sortedEntries.size();
		}
		for (int i = 0; i < safeK; i++) {
			System.out.println(sortedEntries.get(i));
		}
	}

}

