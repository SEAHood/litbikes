package com.litbikes.engine;

import java.util.HashMap;
import java.util.Map;

public class ScoreKeeper {
	
	private final Map<Integer, Integer> scoreCard;
	
	public ScoreKeeper() {
		scoreCard = new HashMap<>();
	}
	
	public int grantScore( int score, int pid ) {
		int currentScore = scoreCard.get(pid) != null ? scoreCard.get(pid) : 0;
		int newScore = currentScore + score;
		scoreCard.put(pid, newScore);
		return newScore;
	}
	
	public int revokeScore( int score, int pid ) {
		int currentScore = scoreCard.get(pid) != null ? scoreCard.get(pid) : 0;
		int newScore = Math.max(currentScore - score, 0);
		scoreCard.put(pid, newScore);
		return newScore;
	}
	
	public int getScore( int pid ) {
		Integer score = scoreCard.get(pid);
		return score != null ? score : 0;
	}
	
	public void reset() {
		scoreCard.clear();
	}
	
}
