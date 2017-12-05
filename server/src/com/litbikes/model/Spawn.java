package com.litbikes.model;

import com.litbikes.engine.GameEngine;
import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Spawn {
	
	private final Vector pos;
	private final Vector dir;
	private final double spd;
	
	public Spawn(int gameSize) {
		pos = createSpawnPosition(gameSize);
		dir = createSpawnDir();
		spd = GameEngine.BASE_BIKE_SPEED;
	}

	public Vector getPos() {
		return pos;
	}

	public Vector getDir() {
		return dir;
	}
	
	public double getSpd() {
		return spd;
	}

	private Vector createSpawnPosition(int gameSize) {
		return new Vector(NumberUtil.randInt(20, gameSize - 20), NumberUtil.randInt(20, gameSize - 20));
	}
	
	private Vector createSpawnDir() {
		int dir = NumberUtil.randInt(1, 4);
		switch (dir) {
			case 1:
				return new Vector(0, -1);
			case 2:
				return new Vector(0, 1);
			case 3:
				return new Vector(-1, 0);
			case 4:
				return new Vector(1, 0);
			default:
				return new Vector(0, 0); // Won't happen
		}
	}
	
}
