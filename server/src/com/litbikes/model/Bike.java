package com.litbikes.model;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.litbikes.dto.BikeDto;
import com.litbikes.util.Vector;

public class Bike {
	//private static Logger LOG = Log.getLogger(Bike.class);	
	private final int ownerPid;
	private Vector pos;
	private Vector dir;
	private double spd;
	private CopyOnWriteArrayList<TrailSegment> trail;
	private Vector startPos;
	private Color colour;
	private Random random = new Random();
	
	public Bike(int ownerPid) {
		this.ownerPid = ownerPid;
	}
	
	public void init(Spawn spawn, boolean newPlayer) {
		pos = spawn.getPos();
		dir = spawn.getDir();
		spd = spawn.getSpd();
		trail = new CopyOnWriteArrayList<>();
		startPos = pos;
		addTrailPoint();
		
		if ( newPlayer )
			colour = generateBikeColour();			
	}
	
	private Color generateBikeColour() 
	{
		String[] colours = {
			"#ff0099",
			"#b4ff69",
			"#69b4ff",
			"#ffb469",
			"#69ffb4",
			"#f3f315",
			"#f1c40f",
			"#e74c3c",
			"#8e44ad",
			"#3498db",
			"#2ecc71",
			"#9b59b6",
			"#27ae60",
			"#1abc9c",
			"#2980b9",
			"#d35400",
			"#f39c12"
		};		
        String colour = colours[random.nextInt(colours.length)];		
        return Color.decode(colour);		
	}

	public void updatePosition() {		
        double xDiff = dir.x * spd;
        double yDiff = dir.y * spd;
		pos.add(new Vector(xDiff, yDiff));
	}
	
	private void addTrailPoint() {
		Line2D segLine;
		if ( trail.size() > 0 ) {
			Line2D lastSeg = trail.get(trail.size() - 1).getLine();
			segLine = new Line2D.Double(lastSeg.getX2(), lastSeg.getY2(), pos.x, pos.y);	
		} else {
			segLine = new Line2D.Double(pos.x, pos.y, pos.x, pos.y);	
		}
		trail.add(new TrailSegment(ownerPid, segLine));
	}
	
	public boolean collides( List<TrailSegment> trail, int lookAhead ) {
		double aheadX = pos.x + (lookAhead * dir.x);
		double aheadY = pos.y + (lookAhead * dir.y);
		
		Line2D line = new Line2D.Double(pos.x, pos.y, aheadX, aheadY);
					
		for ( TrailSegment segment : trail ) {
			if ( line.intersectsLine(segment.getLine()) ) 
				return true;	
		}

		return false;
	}
		
	public BikeDto getDto() {
		BikeDto dto = new BikeDto();
		dto.pos = new Vector(pos.x, pos.y);
		dto.dir = new Vector(dir.x, dir.y);
		dto.spd = spd;
		dto.trail = trail.stream()
                .map(tp -> tp.getDto())
                .collect(Collectors.toList());
		dto.colour = String.format("rgba(%s,%s,%s,%%A%%)", colour.getRed(), colour.getGreen(), colour.getBlue());
		return dto;
	}

	public Vector getPos() {
		return pos;
	}

	public void setPos(Vector pos) {
		this.pos = pos;
	}

	public Point2D getPosAsPoint2D() {
		return new Point2D.Double(pos.x, pos.y);
	}

	public Vector getDir() {
		return dir;
	}

	public double getSpd() {
		return spd;
	}

	public void setSpd(double spd) {
		this.spd = spd;
	}

	public void setDir(Vector dir) {
        if ( ( this.dir.x == 0 && dir.x == 0 ) || ( this.dir.y == 0 && dir.y == 0 ) )
            return;
                
		this.dir = dir;
		addTrailPoint();
	}

	public List<TrailSegment> getTrail( boolean withHead ) {
		if ( !withHead ) 
			return trail.subList(0, Math.max(trail.size() - 1, 0));
		
		List<TrailSegment> trailWithHead = new ArrayList<>(trail);

		if ( trail.size() > 0 ) {
			Line2D lastSeg = trail.get(trail.size() - 1).getLine();
			Line2D headLine = new Line2D.Double(lastSeg.getX2(), lastSeg.getY2(), pos.x, pos.y);		
			trailWithHead.add(new TrailSegment(ownerPid, headLine));
		} else {
			Line2D headLine = new Line2D.Double(startPos.x, startPos.y, pos.x, pos.y);
			trailWithHead.add(new TrailSegment(ownerPid, headLine));
		}
				
		return trailWithHead;
	}
	
	public Color getColour() {
		return colour;
	}
	
	public void crash() {
		this.setDir(Vector.zero());
		addTrailPoint();
	}	
	
	@Override
	public String toString() {
		return "bike s(" + pos.x + ", " + pos.y + "), s(" + dir.x + ", " + dir.y +")";
	}
	
}
