package com.litbikes.model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.litbikes.util.Vector;

public class Trail {
	private final Map<String, TrailSegment> segments;
	
	public Trail() {
		segments = new ConcurrentHashMap<String, TrailSegment>();
	}
	
	public List<TrailSegment> getList() {
		List<TrailSegment> copy = new ArrayList<>();
		for (TrailSegment s : segments.values()) {
			copy.add(s.clone());
		}
		return copy;
	}

	public void add(TrailSegment segment) {
		segments.values().stream().forEach(s -> s.setHead(false));
		segment.setHead(true);
		segments.put(segment.getId(), segment);
	}

	public int size() {
		return segments.values().size();
	}

	public TrailSegment getHead() {
		Collection<TrailSegment> values = segments.values();
		for (TrailSegment s : values) {
			if (s.isHead())
				return s;
		}
		return null;
	}

	public void replaceSegments(TrailSegment s, TrailSegment... newSegments) {
		TrailSegment segment = segments.get(s.getId());
		if (segment == null) 
			return;
		
	}
	
	public void breakSegment(ImpactPoint impactPoint, double radius) {
		TrailSegment requestedSegment = impactPoint.getTrailSegment();
		TrailSegment newSegment1 = null;
		TrailSegment newSegment2 = null;
		Vector orientation = requestedSegment.getOrientation();
		if (orientation.x == 1) {
			// segment is horizontal
			double y = requestedSegment.getLine().getY1();
			double x1 = requestedSegment.getLine().getX1();
			double x2 = requestedSegment.getLine().getX2();
			double t1x1, t1x2, t2x1, t2x2;			
			if (x1 < x2) {
				t1x1 = Math.min(x1, x2);
				t1x2 = Math.max(impactPoint.getPoint().getX() - radius, t1x1);
				t2x2 = Math.max(x1, x2);				
				t2x1 = Math.min(impactPoint.getPoint().getX() + radius, t2x2);
			} else {
				t1x1 = Math.max(x1, x2);
				t1x2 = Math.min(impactPoint.getPoint().getX() + radius, t1x1);
				t2x2 = Math.min(x1, x2);					
				t2x1 = Math.max(impactPoint.getPoint().getX() - radius, t2x2);
			}						
			newSegment1 = new TrailSegment(requestedSegment.getOwnerPid(), new Line2D.Double(t1x1, y, t1x2, y));
			newSegment2 = new TrailSegment(requestedSegment.getOwnerPid(), new Line2D.Double(t2x1, y, t2x2, y));
			
		} else if (orientation.y == 1) {
			// segment is vertical
			double x = requestedSegment.getLine().getX1();
			double y1 = requestedSegment.getLine().getY1();
			double y2 = requestedSegment.getLine().getY2();		
			double t1y1, t1y2, t2y1, t2y2;			
			if (y1 < y2) {
				t1y1 = Math.min(y1, y2);
				t1y2 = Math.max(impactPoint.getPoint().getY() - radius, t1y1);
				t2y2 = Math.max(y1, y2);					
				t2y1 = Math.min(impactPoint.getPoint().getY() + radius, t2y2);
			} else {
				t1y1 = Math.max(y1, y2);
				t1y2 = Math.min(impactPoint.getPoint().getY() + radius, t1y1);	
				t2y2 = Math.min(y1, y2);					
				t2y1 = Math.max(impactPoint.getPoint().getY() - radius, t2y2);
			}
			newSegment1 = new TrailSegment(requestedSegment.getOwnerPid(), new Line2D.Double(x, t1y1, x, t1y2));
			newSegment2 = new TrailSegment(requestedSegment.getOwnerPid(), new Line2D.Double(x, t2y1, x, t2y2));			
		}

		TrailSegment segment = segments.get(requestedSegment.getId());
		if (segment == null) {
			if (requestedSegment.isHead()) {
				segment = requestedSegment; // Segment being broken is (missing) segment between bike and last corner
			} else {
				return;	
			}
		}
		Point2D frontOfSegment = new Point2D.Double(segment.getLine().getX2(), segment.getLine().getY2());
		Point2D newSegment1Point1 = new Point2D.Double(newSegment1.getLine().getX1(), newSegment1.getLine().getY1());
		Point2D newSegment1Point2 = new Point2D.Double(newSegment1.getLine().getX2(), newSegment1.getLine().getY2());
		Point2D newSegment2Point1 = new Point2D.Double(newSegment2.getLine().getX1(), newSegment2.getLine().getY1());
		Point2D newSegment2Point2 = new Point2D.Double(newSegment2.getLine().getX2(), newSegment2.getLine().getY2());
		
		if (segment.isHead()) {
			segments.values().forEach(s -> s.setHead(false));
			if (frontOfSegment.equals(newSegment1Point1) || frontOfSegment.equals(newSegment1Point2)) {
				newSegment1.setHead(true);
			} else if (frontOfSegment.equals(newSegment2Point1) || frontOfSegment.equals(newSegment2Point2)) {
				newSegment2.setHead(true);
			}
		}
		
		segments.remove(requestedSegment.getId());
		segments.put(newSegment1.getId(), newSegment1);
		segments.put(newSegment2.getId(), newSegment2);		
	}
}
