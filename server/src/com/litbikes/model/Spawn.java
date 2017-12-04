package com.litbikes.model;

import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Spawn {
	
	private final Vector pos;
	private final Vector spd;
	
	public Spawn(int gameWidth, int gameHeight) {
		pos = createSpawnPosition(gameWidth, gameHeight);
		spd = createSpawnSpeed();
	}

	public Vector getPos() {
		return pos;
	}

	public Vector getDir() {
		return spd;
	}

	private Vector createSpawnPosition(int gameWidth, int gameHeight) {
		return new Vector(NumberUtil.randInt(20, gameWidth - 20), NumberUtil.randInt(20, gameHeight - 20));
	}
	
	private Vector createSpawnSpeed() {
		Vector spd;
		int dir = NumberUtil.randInt(1, 4);
		switch (dir) {
			case 1:
				spd = new Vector(0, -1);
				break;
			case 2:
				spd = new Vector(0, 1);
				break;
			case 3:
				spd = new Vector(-1, 0);
				break;
			case 4:
				spd = new Vector(1, 0);
				break;
			default:
				spd = new Vector(0, 0); // Won't happen
				break;
		}
		return spd;
	}
	
}
