package com.litbikes.model;

import com.litbikes.dto.TrailSegmentDto;
import com.litbikes.util.Vector;
import java.awt.geom.Line2D;

public class TrailSegment {
	private final int ownerPid;
	private final Line2D line;
	
	public TrailSegment( int ownerPid, Line2D line ) {
		this.ownerPid = ownerPid;
		this.line = line;
	}

	public int getOwnerPid() {
		return ownerPid;
	}
	
	public Line2D getLine() {
		return line;
	}
	
	public TrailSegmentDto getDto() {
		TrailSegmentDto dto = new TrailSegmentDto();
		dto.start = new Vector(line.getX1(), line.getY1());
		dto.end = new Vector(line.getX2(), line.getY2());
		return dto;
	}
}
