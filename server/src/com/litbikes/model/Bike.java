package com.litbikes.model;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import com.litbikes.dto.BikeDto;
import com.litbikes.server.Game;
import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Bike {
	
	private int pid;
	private Vector pos;
	private Vector spd;
	private List<Vector> trail;
	private boolean crashed;
	private boolean spectating;
	
	private Bike(int pid, Vector pos, Vector spd) {
		this.pid = pid;
		this.pos = pos;
		this.spd = spd;
		trail = new ArrayList<>();
		trail.add(new Vector(pos.x, pos.y));
		crashed = false;
		spectating = false;
		addTrailPoint();
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
		if ( !crashed ) {
	        double xDiff = spd.x * Game.SPEED_MAGNITUDE;
	        double yDiff = spd.y * Game.SPEED_MAGNITUDE;
			pos.add(new Vector(xDiff, yDiff));
			//System.out.println(pid + " - new position: "+pos.x+","+pos.y);
		}
	}
	
	public void addTrailPoint() {
		trail.add( new Vector(pos.x, pos.y) );
	}
	
	public boolean checkCollision( List<Vector> trail, boolean isThis ) {
		for ( int i = 0; i < trail.size() - 1; i++ ) {
			// if this is the second last trail segment and the bikes trail belongs to this bike,
			// theres no collision - cannot crash into your last two trail segments
			if ( i + 2 >= trail.size() && isThis )
				return false;
			
			Vector thisV = trail.get(i);
			Vector nextV = trail.get(i+1);
			Line2D line = new Line2D.Double(thisV.x, thisV.y, nextV.x, nextV.y);
			//System.out.println("Comparing (" + thisV.x + ", " + thisV.y + ") and (" + nextV.x + ", " + nextV.y + ")");
			if ( line.intersects(pos.x, pos.y, 2, 2) ) {
				return true;
			}
		}

		return false;
		
	}
		
	public BikeDto getDto() {
		BikeDto dto = new BikeDto();
		dto.pid = pid;
		dto.pos = new Vector(pos.x, pos.y);
		dto.spd = new Vector(spd.x, spd.y);
		dto.trail = trail;
		dto.crashed = crashed;
		dto.spectating = spectating;
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

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean dead) {
		this.crashed = dead;
	}
	
	public void crash() {
		this.setCrashed(true);
		this.setSpd(Vector.zero());
		addTrailPoint();
	}
	
	public boolean isSpectating() {
		return spectating;
	}

	public void setSpectating(boolean spectating) {
		this.spectating = spectating;
	}
	
	
}
