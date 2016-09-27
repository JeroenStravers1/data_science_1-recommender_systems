package com.stravers.userItem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.stravers.userPreferences.UserPreferences;

public class NearestNeighbourExtractor {
	
	private static final String EUCLIDIAN				= "euclidian";
	private static final String COSINE					= "cosine";
	private static final String PEARSON					= "pearson";
	private static final String NO_METHOD_CHOSEN		= "No method specified, all results are lies!";
	private static final Double SIMILARITY_THRESHOLD 	= 0.35;
	private static final int NO_TARGETED_ITEM			= -1;
	
	private int targetUser;
	private int targetItem;
	private int n;
	
	public HashMap<Integer, Double> targetUsersRatings;
	public LinkedHashMap<Integer, Double> nMostSimilarUsersAscending;
	public HashMap<Integer, HashMap<Integer, Double>> mostSimilarUsersRatings;
	
	
	public NearestNeighbourExtractor() { 
		targetUser = 0;
		targetItem = 0;
		n = 0;
		targetUsersRatings = new HashMap<Integer, Double>();
	}
	
	
	public void extractNearestNeighbours(int targettedUser, int targettedItem, String method, int n, UserPreferences userPreferences) {
		this.targetUser = targettedUser;
		this.targetItem = targettedItem;
		this.n = n;
		this.targetUsersRatings = userPreferences.ratingsCollection.get(targettedUser);
		this.nMostSimilarUsersAscending = extractNMostSimilarUsersAboveSimilarityThreshold(userPreferences, method);
		this.mostSimilarUsersRatings = extractMostSimilarUsersRatings(nMostSimilarUsersAscending, userPreferences);
	}

	
	/**
	 * calculates the similarity of compared users to target user, stores the top N results that rated the item.
	 * simply returns the most similar users if no target item was specified. 
	 * @param userPreferences (UserPreferences): an object containing the entire dataset
	 * @param method (String): defines which method to use to calculate similarity (pearson, euclidian, cosine)
	 * @return sortedSimilarityScoresPerComparedUser(LinkedHashMap<Integer, Double>): an ordered list of
	 * 	userid - similarityscores (ascending)
	 */
	private LinkedHashMap<Integer, Double> extractNMostSimilarUsersAboveSimilarityThreshold(UserPreferences userPreferences, String method) {
		LinkedHashMap<Integer, Double> sortedSimilarityScoresPerComparedUser = new LinkedHashMap<Integer, Double>(n);
		Iterator<?> allUsersIterator = userPreferences.ratingsCollection.entrySet().iterator();
	    while (allUsersIterator.hasNext()) {
	        Map.Entry userMovieRatings = (Map.Entry) allUsersIterator.next(); // loop through users
	        Integer comparedUserID = (Integer) userMovieRatings.getKey();
	        if(comparedUserIsNotTargetedUser(comparedUserID)) {
	        	HashMap<Integer, Double> currentComparedUserMovieRatings = (HashMap<Integer, Double>) userMovieRatings.getValue(); // get compared users rated movies
	        	sortedSimilarityScoresPerComparedUser = extractSimilarityBasedOnTargetItem(currentComparedUserMovieRatings, sortedSimilarityScoresPerComparedUser, method, comparedUserID);
	        }
	    }
	    return sortedSimilarityScoresPerComparedUser;
	}
	
	
	/**
	 * checks if a target item was specified, determines nearest neighbours accordingly
	 * @param currentComparedUserMovieRatings (HAshMap<integer, double>): list of the current compared users ratings
	 * @param sortedSimilarityScoresPerComparedUser (HashMap<int, double>): list of userIDs - similarityscores
	 * @param method (String): defines which method to use to calculate similarity (pearson, euclidian, cosine)
	 * @param comparedUserID (int): the id of the current compared user
	 * @return sortedSimilarityScoresPerComparedUser(LinkedHashMap<Integer, Double>): an ordered list of
	 * 	userid - similarityscores (ascending)
	 */
	private LinkedHashMap<Integer, Double> extractSimilarityBasedOnTargetItem(
			HashMap<Integer, Double> currentComparedUserMovieRatings, 
			LinkedHashMap<Integer, Double> sortedSimilarityScoresPerComparedUser, String method, int comparedUserID) {
		if(targetItem > NO_TARGETED_ITEM) {
			if(currentComparedUserMovieRatings.containsKey(targetItem)) {
				Double similarityBetweenTargetAndComparedUser = extractSimilarityFromMethod(currentComparedUserMovieRatings, method);
		        if(similarityBetweenTargetAndComparedUser >= SIMILARITY_THRESHOLD) {
		        		sortedSimilarityScoresPerComparedUser = extractSortedNResults(sortedSimilarityScoresPerComparedUser, comparedUserID, similarityBetweenTargetAndComparedUser);
			    }
			}
		}
		else if (targetItem == NO_TARGETED_ITEM){
	        Double similarityBetweenTargetAndComparedUser = extractSimilarityFromMethod(currentComparedUserMovieRatings, method);
	        if(similarityBetweenTargetAndComparedUser >= SIMILARITY_THRESHOLD) {
	        		sortedSimilarityScoresPerComparedUser = extractSortedNResults(sortedSimilarityScoresPerComparedUser, comparedUserID, similarityBetweenTargetAndComparedUser);
		    }
        }
		return sortedSimilarityScoresPerComparedUser;
	}

	/**
	 * Fills the linkedhashmap with compared users and their similarity scores if size < n and
	 * sorts by value (ascending), compares the first item in this list
	 * to the current compared user's similarity score (and replaces if needed).
	 * @param sortedNResults (LinkedHashMap<Integer, Double>): the current list of n compared
	 * 	users with the highest similarity ratings
	 * @param comparedUserID (Integer): the id of the current user being compared
	 * @param similarityBetweenTargetAndComparedUser (Double): the similarityrating of the 
	 * 	compared user to the target user
	 * @return sortedNResults (LinkedHashMap<Integer, Double>): the current list of n compared
	 * 	users with the highest similarity ratings, edited to replace the least similar
	 * 	compared user entry with the current compared user entry if the current compared user
	 * 	is more similar to the target user
	 */
	private LinkedHashMap<Integer, Double> extractSortedNResults(LinkedHashMap<Integer, Double> sortedNResults, int comparedUserID, double similarityBetweenTargetAndComparedUser){
		if(sortedNResults.size() < n) {
			sortedNResults = addToSortedListOfNComparedUsers(sortedNResults, comparedUserID, similarityBetweenTargetAndComparedUser);
		}
		else {
			sortedNResults = storeNMostSimilarUsersInSortedList(sortedNResults, comparedUserID, similarityBetweenTargetAndComparedUser);
		}
		return sortedNResults;
	}
	
	
	// adds compared user entries to the list
	private LinkedHashMap<Integer, Double> addToSortedListOfNComparedUsers(LinkedHashMap<Integer, Double> sortedNResults, int comparedUserID, double similarityBetweenTargetAndComparedUser) {
		sortedNResults.put(comparedUserID, similarityBetweenTargetAndComparedUser);
		sortedNResults = sortListOfUsersBySimilarityScores(sortedNResults);        
		return sortedNResults;
	}
	
	
	// compares current compared user similarity score to the lowest entry in the current list (first item, ascending order)
	// replaces the lowest value if current score is higher, and re-sorts the list (ascending)
	private LinkedHashMap<Integer, Double> storeNMostSimilarUsersInSortedList(LinkedHashMap<Integer, Double> sortedNResults, int comparedUserID, double similarityBetweenTargetAndComparedUser) {
		Iterator<?> nResultsIterator = sortedNResults.entrySet().iterator();
	    Map.Entry<Integer, Double> lowestSimilarityUserOutOfN = (Map.Entry)nResultsIterator.next(); // get first item
		if (similarityBetweenTargetAndComparedUser > lowestSimilarityUserOutOfN.getValue()) {
			int userIDToBeReplaced = lowestSimilarityUserOutOfN.getKey();
			sortedNResults.remove(userIDToBeReplaced);
			sortedNResults.put(comparedUserID, similarityBetweenTargetAndComparedUser);
			sortedNResults = sortListOfUsersBySimilarityScores(sortedNResults);
		}
		return sortedNResults;
	} 
	
	
	/**
	 * selects and executes the desired similarity calculation strategy, returns similarity scores
	 * @param currentComparedUserMovieRatings (HashMap <Integer, Double>): the compared user's rating (value) per item (key)
	 * @param method (String): the desired strategy for calculating the similarity
	 * @return (double): the similarity between the target and compared user	 
	 * */
	private Double extractSimilarityFromMethod(HashMap<Integer, Double> currentComparedUserMovieRatings, String method) {
		Context context;
		switch(method) {
		case EUCLIDIAN:
			context = new Context(new EuclidianSimilarityCalculator());
			return Context.executeStrategy(targetUsersRatings, currentComparedUserMovieRatings);
		case COSINE:
			context = new Context(new CosineSimilarityCalculator());
			return Context.executeStrategy(targetUsersRatings, currentComparedUserMovieRatings);
		case PEARSON:
			context = new Context(new SinglePassPearsonCorrelationCoefficientCalculator());
			return Context.executeStrategy(targetUsersRatings, currentComparedUserMovieRatings);
		default:
			System.out.println(NO_METHOD_CHOSEN);
			return 0.0;
		}
	}	
	
	
	/**
	 * sorts a linkedhashmap's entries by value. I spent quite some time researching an elegant way to do this,
	 * my being unfamiliar with comparators and the new Stream function did not help. I ended up using a function
	 * I found on StackOverflow, as it was far prettier than the if/else sorting monstrosity I created.
	 * @param randomOrderNUsers (LinkedHashMap<Integer, Double>): an unsorted list of userIDs and similarity scores
	 * @return sortedNUsers (LinkedHashMap<Integer, Double>): the same list, but sorted in ascending order.
	 */
	LinkedHashMap<Integer, Double> sortListOfUsersBySimilarityScores(LinkedHashMap<Integer, Double> randomOrderNUsers) {        
		Comparator<Entry<Integer, Double>> valueComparator = 
				(e1, e2) -> e1.getValue().compareTo(e2.getValue());
		LinkedHashMap<Integer, Double> sortedNUsers = 
			randomOrderNUsers.entrySet().stream().
		    sorted(valueComparator).
		    collect(Collectors.toMap(Entry::getKey, Entry::getValue,
		                             (e1, e2) -> e1, LinkedHashMap::new));	
		return sortedNUsers;
	}
	
	
	/**
	 * extracts the rated movies for the selected N most similar users (creates a subset to prevent having to pass
	 * the entire dataset to the predictor)
	 * @param nMostSimilarUsersAscending (LinkedHashMap<Integer, Double>): A list of userIDs and their similarity scores
	 * @param userPreferences (UserPreferences): An object containing the entire user dataset
	 * @return selectedUserRatings (HashMap<Integer, HashMap<Integer, Double>>): a subset consisting of selected user's rated movies
	 */
	private HashMap<Integer, HashMap<Integer, Double>> extractMostSimilarUsersRatings(
			LinkedHashMap<Integer, Double> nMostSimilarUsersAscending, UserPreferences userPreferences) {
		HashMap<Integer, HashMap<Integer, Double>> selectedUserRatings = new HashMap<Integer, HashMap<Integer, Double>> (n);
		Iterator<?> nUsersIterator = userPreferences.ratingsCollection.entrySet().iterator();
	    while (nUsersIterator.hasNext()) {
	        Map.Entry userIDAndSimilarity = (Map.Entry) nUsersIterator.next(); // loop through users
	        int currentUserID = (int) userIDAndSimilarity.getKey();
	        if(currentUserID != targetUser) {
	        	HashMap<Integer, Double> currentUserRatings = userPreferences.ratingsCollection.get(currentUserID);
	        	selectedUserRatings.put(currentUserID, currentUserRatings);
	        }
	    }
	    return selectedUserRatings;
	}
	
	
	private boolean comparedUserIsNotTargetedUser(int comparedUserID) {
		if(comparedUserID != this.targetUser) {
			return true;
		}
		return false;
	}
}
