package com.litbikes.dto;

import com.litbikes.model.PowerUp.PowerUpType;
import com.litbikes.util.Vector;

public class PowerUpDto {
	public String id;
	public String name;
	public Vector pos;
	public PowerUpType type;
	public boolean collected;
}