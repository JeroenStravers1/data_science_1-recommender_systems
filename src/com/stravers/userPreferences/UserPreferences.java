package com.stravers.userPreferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;

/** 
 * The UserPreferences class implements an application that
 * maps a specified dataset to a HashMap<Integer, HashMap<Integer, Double>> 
 * structure. The dataset should be of type int, int, int or int, int, double,
 * and can be either in csv or tsv format.
 */
public class UserPreferences {

	private static final String DATA_LOAD_COMPLETE 			= "--- Finished loading data from ";
	private static final String ALL_CONTENT_DISPLAYED 		= "--- Finished displaying content.";
	
	private static final int USER_ID 						= 0;
	private static final int MOVIE_ID 						= 1;
	private static final int RATING 						= 2;

	// made public to avoid duplicating the dataset with getter methods
	public HashMap<Integer, HashMap<Integer, Double>> ratingsCollection;
	public Set<Integer> allItemIDs;
	
	public UserPreferences(String dataFileName) throws IOException {
		this.ratingsCollection = new HashMap<Integer, HashMap<Integer,Double>>();
		this.allItemIDs = new TreeSet<Integer>();
		loadUserRatings(dataFileName);
	}
	
	
	private void loadUserRatings(String dataFileName) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(dataFileName);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));		
		String currentLine = null;
		
		while ((currentLine = bufferedReader.readLine()) != null) {
			storeUserRatingData(currentLine);
		}
		bufferedReader.close();
		System.out.println(DATA_LOAD_COMPLETE + dataFileName);
	}
	
	
	/**
	 * Stores users and their ratings of movies in a nested hashmap structure: (HashMap<userID: hashmap<movieID: rating>>)
	 */
	private void storeUserRatingData(String currentLine) {
		Vector<Number> parsedRatingDataPerLine = parseRatingData(currentLine);
		int userID = (int) parsedRatingDataPerLine.get(USER_ID);
		int movieID = (int) parsedRatingDataPerLine.get(MOVIE_ID);
		double rating = (double) parsedRatingDataPerLine.get(RATING);

		if (!this.ratingsCollection.containsKey(userID)) {
			this.ratingsCollection.put(userID, new HashMap<Integer, Double>());
		}
		HashMap<Integer, Double> movieRating = ratingsCollection.get(userID);
		movieRating.put(movieID, rating);
		allItemIDs.add(movieID);
	}
	
	
	/**
	 * Extracts userId, movieId and rating from a tsv or csv String,
	 * returns extracted values as Vector(int,int,double). This method
	 * discards the timestamp found in the MovieLens dataset. Double conversion
	 * is done because ratings in userItem.data are stored as Double.
	 * */
	private Vector<Number> parseRatingData(String currentLine) {
		Vector<Number> parsedData = new Vector<Number>();
		String[] tokens = currentLine.split("[,\t]");
		int userId = Integer.parseInt(tokens[USER_ID]);
		int movieId = Integer.parseInt(tokens[MOVIE_ID]);
		double rating = Double.parseDouble(tokens[RATING]);
		parsedData.add(userId);
		parsedData.add(movieId);
		parsedData.add(rating);
		return parsedData;
	}
	
	
	/**
	 * I based this method on the method shown here: 
	 * http://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
	 * I didn't have enough familiarity with HashMap iteration to know this myself.
	 */
	public void displayContents() {
		Iterator<?> it = this.ratingsCollection.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	    }
		System.out.println(ALL_CONTENT_DISPLAYED);
	}
	
	
	public HashMap<Integer, HashMap<Integer, Double>> getRatingsCollection() {
		return this.ratingsCollection;
	}
}
