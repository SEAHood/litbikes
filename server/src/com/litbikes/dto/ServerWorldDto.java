package com.litbikes.dto;

import java.util.List;

import com.litbikes.model.PowerUp;

public class ServerWorldDto {
	
	public long timestamp;
	public long gameTick;
	public boolean roundInProgress;
	public int timeUntilNextRound;
	public int currentWinner;
	public int roundTimeLeft;
	public List<PlayerDto> players;
	public List<PowerUpDto> powerUps;
	public ArenaDto arena;
}
