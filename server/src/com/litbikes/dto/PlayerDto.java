package com.litbikes.dto;

import com.litbikes.model.Player.PlayerEffect;
import com.litbikes.model.PowerUp.PowerUpType;

public class PlayerDto {
	public int pid;
	public String name;
	public BikeDto bike;
	public boolean spectating;
	public boolean crashed;
	public Integer crashedInto;
	public String crashedIntoName;
	public int score;
	public PowerUpType currentPowerUp;
	public PlayerEffect effect;
}
