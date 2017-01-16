package com.litbikes.model;

import com.litbikes.dto.BikeDto;
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
		pos.add(spd);
		System.out.println(pid + " - new position: "+pos.x+","+pos.y);
	}
		
	public BikeDto getDto() {
		BikeDto dto = new BikeDto();
		System.out.println(spd.x);
		System.out.println(pos.x);
		System.out.println(spd.y);
		System.out.println(pos.y);
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
