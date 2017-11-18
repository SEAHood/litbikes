package com.litbikes.model;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.BikeDto;
import com.litbikes.util.NumberUtil;
import com.litbikes.util.Vector;

public class Bike implements ICollidable {
	private static Logger LOG = Log.getLogger(Bike.class);
	
	private int pid;
	private String name;
	private Vector pos;
	private Vector spd;
	private double spdMag = 1.5;
	private List<TrailSegment> trail;
	private boolean crashed;
	private boolean spectating;
	private Vector startPos;
	private Color colour;
	private ICollidable crashedInto;
	private Random random = new Random();
	
	public Bike(int _pid, String _name) {
		pid = _pid;
		name = _name;
	}

	public void init(Spawn spawn, boolean newPlayer) {
		pos = spawn.getPos();
		spd = spawn.getSpd();
		trail = new ArrayList<>();
		crashed = false;
		spectating = false;
		crashedInto = null;
		startPos = pos;
		addTrailPoint();
		
		if ( newPlayer )
			colour = generateBikeColour();
		
		LOG.info("Bike " + pid + " initialised");		
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

	public void updatePosition(double speedModifier) {
		if ( !crashed ) {			
	        double xDiff = spd.x * spdMag * speedModifier;
	        double yDiff = spd.y * spdMag * speedModifier;
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
	
	public boolean collides( List<TrailSegment> trail, int lookAhead ) {
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
		dto.name = name;
		dto.pos = new Vector(pos.x, pos.y);
		dto.spd = new Vector(spd.x, spd.y);
		dto.spdMag = spdMag;
		dto.trail = trail.stream()
                .map(tp -> tp.getDto())
                .collect(Collectors.toList());
		dto.crashed = crashed;
		dto.crashedInto = crashedInto != null ? crashedInto.getId() : null;
		dto.crashedIntoName = crashedInto != null ? crashedInto.getName() : null;
		dto.spectating = spectating;
		dto.colour = String.format("rgba(%s,%s,%s,%%A%%)", colour.getRed(), colour.getGreen(), colour.getBlue());
		return dto;
	}

	public int getPid() {
		return pid;
	}
	
	@Override
	public int getId() {
		return pid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
        if ( ( this.spd.x == 0 && spd.x == 0 ) || ( this.spd.y == 0 && spd.y == 0 ) )
            return;
                
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

	public Color getColour() {
		return colour;
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


	public ICollidable getCrashedInto() {
		return crashedInto;
	}
	
	public boolean crashedIntoSelf() {
		if (crashedInto == null)
			return false;
		return crashedInto.getId() == pid;
	}

	public void setCrashedInto(ICollidable crashedInto) {
		this.crashedInto = crashedInto;
	}
	
	
	@Override
	public String toString() {
		return pid + ": p(" + pos.x + ", " + pos.y + "), s(" + spd.x + ", " + spd.y +"), " + (crashed?"crashed":"not crashed");
	}
	
}
