package com.stravers.userItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CosineSimilarityCalculator implements SimilarityCalculator{

	// devnote: I used E to display the Summation-character, EXX == (EX)^2, EXiXi == EXi^2.
	// I initially tried to keep these variable names verbose (ie.: sumTargetedUserRatingsXComparedUserRatings 
	// for EXiYi), but eventually reversed this; it made the calculation completely illegible.
	private HashMap<Integer, Double> targetUsersRatings;
	private double EXiYi;
	private double EXiXi;
	private double EYiYi;

	
	@Override
	public Double calculateDistance(HashMap<Integer, Double> targetUsersRatings, HashMap<Integer, Double> currentComparedUserMovieRatings) {
		this.targetUsersRatings = targetUsersRatings;
		this.EXiYi = 0;
		this.EXiXi = 0;
		this.EXiYi = 0;
		return calculateCosineSimilarity(currentComparedUserMovieRatings);
	}

	
	private double calculateCosineSimilarity(HashMap<Integer, Double> currentComparedUserMovieRatings) {
	    double similarity = 0f;
		Iterator<?> targettedUsersRatingsIterator = this.targetUsersRatings.entrySet().iterator(); // loop through TUsers rated movies
	    while (targettedUsersRatingsIterator.hasNext()) {
	        Map.Entry targetUsersRatedMovies = (Map.Entry)targettedUsersRatingsIterator.next(); 
	        int currentTargetedUsersMovieToCheckFor = (int) targetUsersRatedMovies.getKey();
	        double targetedUserRating = this.targetUsersRatings.get(currentTargetedUsersMovieToCheckFor);
	        
	        if(currentComparedUserMovieRatings.containsKey(currentTargetedUsersMovieToCheckFor)) {
	        	double comparedUserRating = currentComparedUserMovieRatings.get(currentTargetedUsersMovieToCheckFor);
	        	this.EXiYi += (targetedUserRating * comparedUserRating);
	        	this.EYiYi += (comparedUserRating * comparedUserRating);
	        }
	        
	        this.EXiXi += (targetedUserRating * targetedUserRating);
	        similarity = (EXiYi / (Math.sqrt(EXiXi) * Math.sqrt(EYiYi)));
	    }
	    return similarity;
	}
}
 