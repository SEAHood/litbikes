package com.litbikes.model;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.litbikes.dto.BikeDto;
import com.litbikes.util.Vector;

public class Bike {
	//private static Logger LOG = Log.getLogger(Bike.class);	
	private final int ownerPid;
	private Vector pos;
	private Vector dir;
	private double spd;
	private Trail trail;
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
		trail = new Trail();
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
			Line2D lastSeg = trail.getHead().getLine();
			segLine = new Line2D.Double(lastSeg.getX2(), lastSeg.getY2(), pos.x, pos.y);	
		} else {
			segLine = new Line2D.Double(pos.x, pos.y, pos.x, pos.y);	
		}
		trail.add(new TrailSegment(ownerPid, segLine));
	}
	
	public boolean collides( List<TrailSegment> trail, int lookAhead ) {
		if (trail == null) return false;
		
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
		dto.trail = trail.getList().stream()
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

	public List<TrailSegment> getTrailSegmentList(boolean withHead) {
		if (!withHead) 
			return trail.getList();
		
		List<TrailSegment> trailWithHead = new ArrayList<>(trail.getList());		
		double headSegmentStartX = 0;
		double headSegmentStartY = 0;
		if ( trail.size() > 0 ) {
			TrailSegment head = trail.getHead();
			if (head == null) {
				return null;
			}
			Line2D lastSeg = trail.getHead().getLine();
			headSegmentStartX = lastSeg.getX2();
			headSegmentStartY = lastSeg.getY2();
		} else {
			headSegmentStartX = startPos.x;
			headSegmentStartY = startPos.y;
		}
		
		trailWithHead.stream().forEach(x -> x.setHead(false));
		TrailSegment headSegment = new TrailSegment(ownerPid, new Line2D.Double(headSegmentStartX, headSegmentStartY, pos.x, pos.y));
		headSegment.setHead(true);
		trailWithHead.add(headSegment);
		
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

	public void breakTrailSegment(TrailSegment segment, TrailSegment newSegment1, TrailSegment newSegment2) {
		trail.breakSegment(segment, newSegment1, newSegment2);
	}

	public void breakTrailSegment(ImpactPoint impactPoint, double radius) {
		trail.breakSegment(impactPoint, radius);
	}
	
}
