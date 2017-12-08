package com.litbikes.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import com.litbikes.dto.ScoreDto;

public class ScoreKeeper {
	private final List<ScoreDto> scores;
	
	public ScoreKeeper() {
		scores = new ArrayList<>();
	}
	
	public void grantScore( int pid, String name, int score ) {
		ScoreDto currentScore;
		try {
			currentScore = scores.stream().filter(x -> x.pid == pid).findFirst().get();
		} catch (NoSuchElementException ex) {
			currentScore = new ScoreDto(pid, name, 0);
			scores.add(currentScore);
		}
		
		ScoreDto newScore = new ScoreDto(pid, name, currentScore.score + score);
		scores.set(scores.indexOf(currentScore), newScore);
	}
	
	public int getScore( int pid ) {
		ScoreDto score;
		try {
			score = scores.stream().filter(x -> x.pid == pid).findFirst().get();
		} catch (NoSuchElementException ex) {
			return 0;
		}
		return score != null ? score.score : 0;
	}
	
	public void removeScore(int pid) {
		ScoreDto score;
		try {
			score = scores.stream().filter(x -> x.pid == pid).findFirst().get();
		} catch (NoSuchElementException ex) {
			return;
		}
		scores.remove(score);
	}
	
	public List<ScoreDto> getScores() {
		return scores;
	}
	
	public void reset() {
		scores.clear();
	}

	public int getCurrentWinner() {
		if (scores.size() <= 0) return -1;
		Collections.sort(scores, new SortByScore());
		return scores.get(0).pid;
	}
	
	class SortByScore implements Comparator<ScoreDto>
	{
	    public int compare(ScoreDto a, ScoreDto b)
	    {
	        return b.score - a.score;
	    }
	}

	public String getCurrentWinnerName() {
		if (scores.size() <= 0) return "Unknown";
		Collections.sort(scores, new SortByScore());
		return scores.get(0).name;
	}
	
}
