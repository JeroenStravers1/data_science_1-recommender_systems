package com.stravers.userPreferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.stravers.itemItem.Benchmarker;
import com.stravers.itemItem.Utilities;
import com.stravers.itemItem.WeightedSlopeOneDeviationTableCalculator;
import com.stravers.itemItem.WeightedSlopeOneRatingPredictor;
import com.stravers.userItem.UserRatingPredictor;




public class Main {
	/**
	 * please note that I chucked this MAIN class in my code at an arbitrary position. 
	 * This program would normally be an executable, or part of a larger whole.*/
	private static final String MOVIELENS_DATASET			= "D:/Eclipse Workspace/Data Science assignments/src/u.data";
	private static final String USER_ITEM_DATASET			= "D:/Eclipse Workspace/Data Science assignments/src/userItem.data";
	
	private static final int TARGET_MOVIE_ID 			= 106;
	private static final int NO_TARGET_MOVIE 			= -1; // use this to predict top K movies
	private static final int USER_ID 					= 186;
	private static final String METHOD 					= "pearson";
	private static final int N 							= 25;
	private static final int K							= 5;
	
	
	public static void main (String[] args) throws IOException {
		Benchmarker bm = new Benchmarker();
		// to use the userItem dataset
		//UserPreferences allUserPreferences = new UserPreferences(USER_ITEM_DATASET);	

		// to use the movielens dataset 
		UserPreferences allUserPreferences = new UserPreferences(MOVIELENS_DATASET);
		
		WeightedSlopeOneRatingPredictor wSORP = new WeightedSlopeOneRatingPredictor(allUserPreferences);
		bm.startTimer();
		//wSORP.weightedSlopeOneDeviatonTableCalculator.insertIntoDeviationTable(3, 105, 4.0);
		
		
		
		wSORP.predictTopKRecommendedItems(USER_ID, K);
		//double predictedRating = wSORP.predictRating(USER_ID, TARGET_MOVIE_ID);
		bm.stopTimer();
		Utilities.printMessage(bm.getElapsedTime());
		//System.out.println(predictedRating);
		
	}
}
