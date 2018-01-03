package com.litbikes.engine;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.litbikes.model.ImpactPoint;
import com.litbikes.model.TrailSegment;

public class Physics {
	
	public static ImpactPoint findClosestImpactPoint(Point2D origin, Line2D ray, List<TrailSegment> segments) {
		List<ImpactPoint> impactPoints = new ArrayList<>();
		for (TrailSegment seg : segments) {
			Point2D p = getLineIntersection(ray, seg.getLine());
			if (p != null) {
				impactPoints.add(new ImpactPoint(seg, p));
			}
		}
		
		ImpactPoint closestImpactPoint = null;
		double closestDistance = -1;		
		for (ImpactPoint ip : impactPoints) {
			double distance = origin.distance(ip.getPoint());
			if (distance < closestDistance || closestDistance == -1) {
				closestDistance = distance;
				closestImpactPoint = ip;
			}
		}
		
		return closestImpactPoint;
	}
	
	public static Point2D findClosestIntersection(Point2D origin, Line2D ray, List<Line2D> lines) {
		List<Point2D> points = new ArrayList<>();
		for (Line2D line : lines) {
			Point2D p = getLineIntersection(ray, line);
			if (p != null) {
				points.add(p);
			}
		}
		
		Point2D closestPoint = null;
		double closestDistance = -1;		
		for (Point2D point : points) {
			double distance = origin.distance(point);
			if (distance < closestDistance || closestDistance == -1) {
				closestDistance = distance;
				closestPoint = point;
			}
		}
		
		return closestPoint;
	}
	
	public static Point2D getLineIntersection(Line2D line1, Line2D line2) {
		return lineIntersect(
			(float)line1.getX1(), 
			(float)line1.getY1(),
			(float)line1.getX2(),
			(float)line1.getY2(),
			(float)line2.getX1(),
			(float)line2.getY1(), 
			(float)line2.getX2(),
			(float)line2.getY2()
		);
	}
	
	 public static Point2D lineIntersect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (denom == 0.0) { // Lines are parallel.
			return null;
		}
		double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))/denom;
		double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))/denom;
		if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
			// Get the intersection point.
			return new Point2D.Float((int) (x1 + ua*(x2 - x1)), (int) (y1 + ua*(y2 - y1)));
		}
		return null;
	}
}
