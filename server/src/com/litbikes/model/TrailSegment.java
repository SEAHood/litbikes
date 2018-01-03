package com.litbikes.model;

import com.litbikes.dto.TrailSegmentDto;
import com.litbikes.util.GeometryUtil;
import com.litbikes.util.Vector;
import java.awt.geom.Line2D;
import java.util.UUID;

public class TrailSegment implements Cloneable {
	private final String id;
	private final int ownerPid;
	private final Line2D line;
	private final Vector orientation;
	private boolean isHead;
	
	public TrailSegment(int ownerPid, Line2D line) {
		this(UUID.randomUUID().toString(), ownerPid, line, GeometryUtil.getLineOrientation(line), false);
	}
	
	private TrailSegment(String id, int ownerPid, Line2D line, Vector orientation, boolean isHead) {
		this.id = id;
		this.ownerPid = ownerPid;
		this.line = line;
		this.orientation = orientation;
		this.isHead = isHead;
	}

	public String getId() {
		return id;
	}
	
	public int getOwnerPid() {
		return ownerPid;
	}
	
	public Line2D getLine() {
		return line;
	}
	
	public Vector getOrientation() {
		return orientation;
	}

	public boolean isHead() {
		return isHead;
	}

	public void setHead(boolean isHead) {
		this.isHead = isHead;
	}	
		
	public TrailSegmentDto getDto() {
		TrailSegmentDto dto = new TrailSegmentDto();
		dto.isHead = isHead;
		dto.start = new Vector(line.getX1(), line.getY1());
		dto.end = new Vector(line.getX2(), line.getY2());
		return dto;
	}
	
	@Override
	public TrailSegment clone() {
		return new TrailSegment(id, ownerPid, line, orientation, isHead);
	}
}
