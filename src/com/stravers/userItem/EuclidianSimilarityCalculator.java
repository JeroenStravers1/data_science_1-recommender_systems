package com.stravers.userItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EuclidianSimilarityCalculator implements SimilarityCalculator{

	private HashMap<Integer, Double> targetUsersRatings;
	
	
	@Override
	public Double calculateDistance( HashMap<Integer, Double> targetUsersRatings, HashMap<Integer, Double> currentComparedUserMovieRatings) {
		this.targetUsersRatings = targetUsersRatings;
		return calculateEuclidianSimilarity(currentComparedUserMovieRatings);
	}
	
	
	/**
	 * calculates the euclidian distance and converts it to similarity. Returns similarity.
	 * @param currentComparedUserMovieRatings (HashMap <Integer, Double>): the compared user's rating (value) per item (key)
	 * @return double similarity: the euclidian distance converted to similarity 
	 */
	private double calculateEuclidianSimilarity(HashMap<Integer, Double> currentComparedUserMovieRatings) {
		float totalSquaredRatingDifferencesBetweenTargetAndCurrentUser = calculateEuclidianDistance(currentComparedUserMovieRatings);
		double euclidianDistanceBetweenTargetAndCompared = Math.sqrt(totalSquaredRatingDifferencesBetweenTargetAndCurrentUser);
	    double similarity = 1 / (1 + euclidianDistanceBetweenTargetAndCompared);
	    return similarity;
	}
	
	
	/**
	 * tries to find the targeted users movieid in the current compared user's rated movies. Subtracts the targetUsers rating from 
	 * the compared users rating, and calculates the total euclidian distance between the two.
	 * @param currentComparedUserMovieRatings (HashMap <Integer, Double>): the compared user's rating (value) per item (key)
	 * @return float totalSquaredRatingDifferencesBetweenTargetAndCurrentUser: the total squared difference in ratings between the compared users
	 */
	private float calculateEuclidianDistance(HashMap<Integer, Double> currentComparedUserMovieRatings) {
        float totalSquaredRatingDifferencesBetweenTargetAndCurrentUser = 0;

		Iterator<?> targettedUsersRatingsIterator = this.targetUsersRatings.entrySet().iterator(); // loop through TUsers rated movies
	    while (targettedUsersRatingsIterator.hasNext()) {
	        Map.Entry targetUsersRatedMovies = (Map.Entry)targettedUsersRatingsIterator.next(); 
	        int currentTargetedUsersMovieToCheckFor = (int) targetUsersRatedMovies.getKey();
	        
	        if(currentComparedUserMovieRatings.containsKey(currentTargetedUsersMovieToCheckFor)) {
	        	double targetUsersRating = this.targetUsersRatings.get(currentTargetedUsersMovieToCheckFor);
	        	double comparedUsersRating = currentComparedUserMovieRatings.get(currentTargetedUsersMovieToCheckFor);
	        	double differenceInRatings = (comparedUsersRating - targetUsersRating);
	        	float squaredRatingDifference = (float) (differenceInRatings * differenceInRatings);
	        	totalSquaredRatingDifferencesBetweenTargetAndCurrentUser += squaredRatingDifference; 
	        }
	    }
	    return totalSquaredRatingDifferencesBetweenTargetAndCurrentUser;
	}

}
