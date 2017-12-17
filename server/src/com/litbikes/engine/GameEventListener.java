package com.litbikes.engine;

import java.util.List;

import com.litbikes.dto.ScoreDto;
import com.litbikes.model.Player;

public interface GameEventListener {
	void playerCrashed(Player player);
	void playerSpawned(int pid);
	void scoreUpdated(List<ScoreDto> scores);
	void gameStarted();
	void roundStarted();
	void roundEnded();
}