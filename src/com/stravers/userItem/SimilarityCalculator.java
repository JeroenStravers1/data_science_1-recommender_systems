package com.stravers.userItem;

import java.util.HashMap;

public interface SimilarityCalculator {
	
	Double calculateDistance(HashMap<Integer, Double> targetUsersRatings, 
			HashMap<Integer, Double> currentComparedUserMovieRatings);
}
