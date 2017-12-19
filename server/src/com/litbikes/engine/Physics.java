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
	
	public static Point2D getLineIntersection(Line2D ray, Line2D segment) {
	    if (ray.intersectsLine(segment)) {
	        double rx1 = ray.getX1(),
                ry1 = ray.getY1(),
                rx2 = ray.getX2(),
                ry2 = ray.getY2(),
                sx1 = segment.getX1(),
                sy1 = segment.getY1(),
                sx2 = segment.getX2(),
                sy2 = segment.getY2(),
                rdx = rx2 - rx1,
                rdy = ry2 - ry1,
                sdx = sx2 - sx1,
                sdy = sy2 - sy1,
                t1, t2,
                ix, iy;

	        t2 = (rdx * (sy1 - ry1) + rdy * (rx1 - sx1)) / (sdx * rdy - sdy * rdx);
	        t1 = (sx1 + sdx * t2 - rx1) / rdx;

	        if (t1 > 0/* && 1 > t2 && t2 > 0*/) {
	            ix = rx1 + rdx * t1;
	            iy = ry1 + rdy * t1;
	            return new Point2D.Float((int) ix, (int) iy);
	        } else
	            return null;
	    } else
	        return null;
	}
}
