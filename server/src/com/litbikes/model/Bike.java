package com.litbikes.model;

import com.litbikes.dto.BikeDto;
import com.litbikes.game.Game;
import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Bike {
	
	private int pid;
	private Vector pos;
	private Vector spd;
	private boolean dead;
	
	private Bike(int pid, Vector pos, Vector spd) {
		this.pid = pid;
		this.pos = pos;
		this.spd = spd;
		dead = false;
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

	public void updatePosition() {
        double xDiff = spd.x * Game.SPEED_MAGNITUDE;
        double yDiff = spd.y * Game.SPEED_MAGNITUDE;
		pos.add(new Vector(xDiff, yDiff));
		//System.out.println(pid + " - new position: "+pos.x+","+pos.y);
	}
		
	public BikeDto getDto() {
		BikeDto dto = new BikeDto();
		dto.pid = pid;
		dto.pos = new Vector(pos.x, pos.y);
		dto.spd = new Vector(spd.x, spd.y);
		dto.dead = dead;
		return dto;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public Vector getPos() {
		return pos;
	}

	public void setPos(Vector pos) {
		this.pos = pos;
	}

	public Vector getSpd() {
		return spd;
	}

	public void setSpd(Vector spd) {
		this.spd = spd;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	
	
	
}
