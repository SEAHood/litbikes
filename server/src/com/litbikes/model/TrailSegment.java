package com.litbikes.model;

import com.litbikes.dto.TrailSegmentDto;
import com.litbikes.util.Vector;
import java.awt.geom.Line2D;

public class TrailSegment {
	private Line2D line;
	
	public TrailSegment( Line2D line ) {
		this.line = line;
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
