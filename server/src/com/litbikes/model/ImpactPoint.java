package com.litbikes.model;

import java.awt.geom.Point2D;

public class ImpactPoint {
	private TrailSegment trailSegment;
	private Point2D point;
	
	public ImpactPoint(TrailSegment seg, Point2D p) {
		trailSegment = seg;
		point = p;
	}
	
	public TrailSegment getTrailSegment() {
		return trailSegment;
	}
	public void setTrailSegment(TrailSegment trailSegment) {
		this.trailSegment = trailSegment;
	}
	public Point2D getPoint() {
		return point;
	}
	public void setPoint(Point2D point) {
		this.point = point;
	}	
}
