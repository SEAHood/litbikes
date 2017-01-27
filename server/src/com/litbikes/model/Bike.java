package com.litbikes.model;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.litbikes.dto.BikeDto;
import com.litbikes.util.ColourUtil;
import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Bike {
	
	private int pid;
	private Vector pos;
	private Vector spd;
	private double spdMag = 1;
	private List<TrailSegment> trail;
	private boolean crashed;
	private boolean spectating;
	private Vector startPos;
	private Color colour;
	private String crashedInto;
	
	public Bike(int pid) {
		this.pid = pid;
	}

	public void init() {
		Vector position = new Vector(NumberUtil.randInt(20, 480), NumberUtil.randInt(20, 480));
		pos = position;
		startPos = position;
		spd = null;

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

		trail = new ArrayList<>();
		crashed = false;
		spectating = false;
		colour = ColourUtil.getBrightColor();
		addTrailPoint();
		
	}

	public void updatePosition() {
		if ( !crashed ) {
	        double xDiff = spd.x * spdMag;
	        double yDiff = spd.y * spdMag;
			pos.add(new Vector(xDiff, yDiff));
		}
	}
	
	private void addTrailPoint() {
		Line2D segLine;
		if ( trail.size() > 0 ) {
			Line2D lastSeg = trail.get(trail.size() - 1).getLine();
			segLine = new Line2D.Double(lastSeg.getX2(), lastSeg.getY2(), pos.x, pos.y);	
		} else {
			segLine = new Line2D.Double(pos.x, pos.y, pos.x, pos.y);	
		}
		trail.add(new TrailSegment(segLine));
	}
	
	public boolean checkCollision( List<TrailSegment> trail, int lookAhead ) {
		double aheadX = pos.x + (lookAhead * spd.x);
		double aheadY = pos.y + (lookAhead * spd.y);
		
		Line2D line = new Line2D.Double(pos.x, pos.y, aheadX, aheadY);
					
		for ( TrailSegment segment : trail ) {
			if ( line.intersectsLine(segment.getLine()) ) 
				return true;	
		}

		return false;
		
	}
		
	public BikeDto getDto() {
		BikeDto dto = new BikeDto();
		dto.pid = pid;
		dto.pos = new Vector(pos.x, pos.y);
		dto.spd = new Vector(spd.x, spd.y);
		dto.spdMag = spdMag;
		dto.trail = trail.stream()
                .map(tp -> tp.getDto())
                .collect(Collectors.toList());
		dto.crashed = crashed;
		dto.crashedInto = crashedInto;
		dto.spectating = spectating;
		dto.colour = String.format("rgba(%s,%s,%s,%%A%%)", colour.getRed(), colour.getGreen(), colour.getBlue());
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
        if ( ( this.spd.x == 0 && spd.x == 0 ) || ( this.spd.y != 0 && spd.y != 0 ) ) {
            return;
        }		
                
		this.spd = spd;
		addTrailPoint();
	}
	
	

	public List<TrailSegment> getTrail( boolean withHead ) {
		if ( !withHead ) 
			return trail.subList(0, Math.max(trail.size() - 1, 0));
		
		List<TrailSegment> trailWithHead = new ArrayList<>(trail);
		
		if ( trail.size() > 0 ) {
			Line2D lastSeg = trailWithHead.get(trail.size() - 1).getLine();
			Line2D headLine = new Line2D.Double(lastSeg.getX2(), lastSeg.getY2(), pos.x, pos.y);		
			trailWithHead.add(new TrailSegment(headLine));
		} else {
			Line2D headLine = new Line2D.Double(startPos.x, startPos.y, pos.x, pos.y);
			trailWithHead.add(new TrailSegment(headLine));
		}
		
		
		return trailWithHead;
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
	
	public boolean isActive() {
		return !isCrashed() && !isSpectating();
	}

	public void setCrashedInto(String crashedInto) {
		this.crashedInto = crashedInto;
	}

	
	
}
