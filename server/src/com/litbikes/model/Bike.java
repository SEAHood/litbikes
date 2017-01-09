package com.litbikes.model;

import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Bike {
	
	private int pid;
	private Vector pos;
	private Vector spd;
	
	private Bike(int pid, Vector pos, Vector spd) {
		this.pid = pid;
		this.pos = pos;
		this.spd = spd;
	}

	public static Bike create(int pid) {
		Vector pos = new Vector(NumberUtil.randInt(20, 480), NumberUtil.randInt(20, 480));
		Vector spd = null;

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
				break;
		}

		return new Bike(pid, pos, spd);
	}
	
}
