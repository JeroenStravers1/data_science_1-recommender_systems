package com.stravers.userItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SinglePassPearsonCorrelationCoefficientCalculator implements SimilarityCalculator{
	
	// devnote: I used E to display the Summation-character, EXX == (EX)^2, EXiXi == EXi^2.
	// I initially tried to keep these variable names verbose (ie.: sumTargetedUserRatingsXComparedUserRatings 
	// for EXiYi), but eventually reversed this; it made the calculation completely illegible. 
	private double EXiYi;
	private double EXi;
	private double EYi;
	private double EXiXi;
	private double EXX;
	private double EYiYi;
	private double EYY;
	private int n;
	private HashMap<Integer, Double> targetUsersRatings;
	
	
	/**
	 *	resets/initializes class variables, calculates the approximate pearson correlation coefficient. 
	 */
	@Override
	public Double calculateDistance(HashMap<Integer, Double> targetUsersRatings, HashMap<Integer, Double> currentComparedUserMovieRatings){
		this.EXiYi = 0;
		this.EXi = 0;
		this.EYi = 0;
		this.EXiXi = 0;
		this.EXX = 0;
		this.EYiYi = 0;
		this.EYY = 0;
		this.n = 0;
		this.targetUsersRatings = targetUsersRatings;
		return calculateApproximatePearsonCorrelationCoefficient(currentComparedUserMovieRatings);
	}
	
	
	private double calculateApproximatePearsonCorrelationCoefficient(HashMap<Integer, Double> currentComparedUserMovieRatings) {		
		Iterator<?> targettedUsersRatingsIterator = this.targetUsersRatings.entrySet().iterator(); // loop through TUsers rated movies
	    while (targettedUsersRatingsIterator.hasNext()) {
	        Map.Entry targetUsersRatedMovies = (Map.Entry)targettedUsersRatingsIterator.next(); 
	        int currentTargetedUsersMovieToCheckFor = (int) targetUsersRatedMovies.getKey();
	        
	        if(currentComparedUserMovieRatings.containsKey(currentTargetedUsersMovieToCheckFor)) {
	        	double targetedUserRating = this.targetUsersRatings.get(currentTargetedUsersMovieToCheckFor);
	        	double comparedUserRating = currentComparedUserMovieRatings.get(currentTargetedUsersMovieToCheckFor);
	        	this.EXiYi += (targetedUserRating * comparedUserRating);
	        	this.EXi += targetedUserRating;
	        	this.EYi += comparedUserRating;
	        	this.EXiXi += (targetedUserRating * targetedUserRating);
	        	this.EYiYi += (comparedUserRating * comparedUserRating);
	        	this.n += 1;
	        }
	    }
	    this.EXX = EXi * EXi;
	    this.EYY = EYi * EYi;
	    
	    double approximateCorrelationCoefficient = calculateCoefficient();
	    return approximateCorrelationCoefficient;
	}
	
	
	
	private double calculateCoefficient() {
		double approximateCorrelationCoefficient = ( 
				(EXiYi - ( (EXi * EYi) / n) )
				/ (Math.sqrt( (EXiXi - (EXX / n) ) ) * (Math.sqrt( ( EYiYi - ( EYY / n ) ) ) ) )
		);
		return approximateCorrelationCoefficient;
	}
	
}
