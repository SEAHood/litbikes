package com.litbikes.model;

import java.util.ArrayList;
import java.util.List;

import com.litbikes.dto.BikeDto;
import com.litbikes.game.Game;
import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Bike {
	
	private int pid;
	private Vector pos;
	private Vector spd;
	private List<Vector> trail;
	private boolean dead;
	
	private Bike(int pid, Vector pos, Vector spd) {
		this.pid = pid;
		this.pos = pos;
		this.spd = spd;
		trail = new ArrayList<>();
		trail.add(new Vector(pos.x, pos.y));
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
	
	public void addTrailPoint() {
		trail.add( new Vector(pos.x, pos.y) );
	}
		
	public BikeDto getDto() {
		BikeDto dto = new BikeDto();
		dto.pid = pid;
		dto.pos = new Vector(pos.x, pos.y);
		dto.spd = new Vector(spd.x, spd.y);
		dto.trail = trail;
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

	// Returns true if speed is changed
	public boolean setSpd(Vector spd) {
        if ( ( this.spd.x == 0 && spd.x == 0 ) || ( this.spd.y != 0 && spd.y != 0 ) ) {
            return false;
        }		
		this.spd = spd;
		return true;
	}
	
	

	public List<Vector> getTrail() {
		return trail;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	
	
	
}
