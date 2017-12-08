package com.litbikes.dto;

import java.util.List;

public class ServerWorldDto {
	
	public long timestamp;
	public long gameTick;
	public boolean roundInProgress;
	public int timeUntilNextRound;
	public int currentWinner;
	public int roundTimeLeft;
	public List<BikeDto> bikes;
	public ArenaDto arena;
}
