package com.stravers.userItem;

import java.util.HashMap;

//devnote: I copied this class from http://www.tutorialspoint.com/design_pattern/strategy_pattern.htm, 
//and I'm not ashamed. I was almost there myself, I had function selection based on an input string (still do, but nicer),
//and was about to put my different algorithms into separate classes.
//I finally googled strategy pattern, found this example, and it all came together. I do believe in giving credit where
//it's due; I owe my current implementation of the strategy pattern in part to this site! 
public class Context {
   private static SimilarityCalculator strategy;

   
   public Context(SimilarityCalculator strategy){
      this.strategy = strategy;
   }

   
   public static Double executeStrategy(HashMap<Integer, Double> targetUsersRatings, HashMap<Integer, Double> currentComparedUserMovieRatings){
      return strategy.calculateDistance(targetUsersRatings, currentComparedUserMovieRatings);
   }
}