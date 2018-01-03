package com.litbikes.tests;

import static org.junit.Assert.*;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.litbikes.engine.Physics;
import com.litbikes.model.ImpactPoint;
import com.litbikes.model.TrailSegment;

public class PhysicsTest {

    @Test
    public void getLineIntersection_should_correctly_return_intersection_point() {
    	Line2D line1 = new Line2D.Float(0f, 0f, 20f, 20f);
    	Line2D line2 = new Line2D.Float(0f, 20f, 20f, 0f);
    	Point2D p = Physics.getLineIntersection(line1, line2);
    	assertEquals(p.getX(), 10, 0.1);
    	assertEquals(p.getY(), 10, 0.1);
    	line1 = new Line2D.Float(20f, 20f, 0f, 0f);
    	line2 = new Line2D.Float(20f, 0f, 0f, 20f);
    	p = Physics.getLineIntersection(line1, line2);
    	assertEquals(p.getX(), 10, 0.1);
    	assertEquals(p.getY(), 10, 0.1);

    	line1 = new Line2D.Float(0f, 0f, 20f, 10f);
    	line2 = new Line2D.Float(0f, 10f, 20f, 0f);
    	p = Physics.getLineIntersection(line1, line2);
    	assertEquals(p.getX(), 10, 0.1);
    	assertEquals(p.getY(), 5, 0.1);
    	line1 = new Line2D.Float(20f, 10f, 0f, 0f);
    	line2 = new Line2D.Float(20f, 0f, 0f, 10f);
    	p = Physics.getLineIntersection(line1, line2);
    	assertEquals(p.getX(), 10, 0.1);
    	assertEquals(p.getY(), 5, 0.1);

    	line1 = new Line2D.Float(0f, 0f, 10f, 20f);
    	line2 = new Line2D.Float(0f, 20f, 10f, 0f);
    	p = Physics.getLineIntersection(line1, line2);
    	assertEquals(p.getX(), 5, 0.1);
    	assertEquals(p.getY(), 10, 0.1);   
    	line1 = new Line2D.Float(10f, 20f, 0f, 0f);
    	line2 = new Line2D.Float(10f, 0f, 0f, 20f);
    	p = Physics.getLineIntersection(line1, line2);
    	assertEquals(p.getX(), 5, 0.1);
    	assertEquals(p.getY(), 10, 0.1);    	
    }
    
    @Test
    public void findClosestImpactPoint_should_return_correct_impact_point() {
    	Point2D origin = new Point2D.Float(50, 50);
    	
    	// facing right ->
    	Line2D ray = new Line2D.Float((float)origin.getX(), (float)origin.getY(), 600f, (float)origin.getY());    	
    	// 3 vertical lines at x = 60, 70, 80
    	TrailSegment segment1 = new TrailSegment(0, new Line2D.Float(60f, 0f, 60f, 100f));
    	TrailSegment segment2 = new TrailSegment(0, new Line2D.Float(70f, 0f, 70f, 100f));
    	TrailSegment segment3 = new TrailSegment(0, new Line2D.Float(80f, 0f, 80f, 100f));
    	List<TrailSegment> segments = new ArrayList<TrailSegment>(Arrays.asList(segment1, segment2, segment3));
    	ImpactPoint impact = Physics.findClosestImpactPoint(origin, ray, segments);
    	assertNotNull(impact);
    	assertEquals(60f, impact.getPoint().getX(), 0.1);
    	assertEquals(50f, impact.getPoint().getY(), 0.1);
    	
    	// facing left <-
    	ray = new Line2D.Float((float)origin.getX(), (float)origin.getY(), 0f, (float)origin.getY());    	
    	// 3 vertical lines at x = 20, 30, 40
    	segment1 = new TrailSegment(0, new Line2D.Float(20f, 0f, 20f, 100f));
    	segment2 = new TrailSegment(0, new Line2D.Float(30f, 0f, 30f, 100f));
    	segment3 = new TrailSegment(0, new Line2D.Float(40f, 0f, 40f, 100f));
    	segments = new ArrayList<TrailSegment>(Arrays.asList(segment1, segment2, segment3));
    	impact = Physics.findClosestImpactPoint(origin, ray, segments);
    	assertNotNull(impact);
    	assertEquals(40f, impact.getPoint().getX(), 0.1);
    	assertEquals(50f, impact.getPoint().getY(), 0.1);
    	
    	// facing up ^
    	ray = new Line2D.Float((float)origin.getX(), (float)origin.getY(), (float)origin.getX(), 0f);    	
    	// 3 horizontal lines at y = 20, 30, 40
    	segment1 = new TrailSegment(0, new Line2D.Float(0f, 20f, 100f, 20f));
    	segment2 = new TrailSegment(0, new Line2D.Float(0f, 30f, 100f, 30f));
    	segment3 = new TrailSegment(0, new Line2D.Float(0f, 40f, 100f, 40f));
    	segments = new ArrayList<TrailSegment>(Arrays.asList(segment1, segment2, segment3));
    	impact = Physics.findClosestImpactPoint(origin, ray, segments);
    	assertNotNull(impact);
    	assertEquals(50f, impact.getPoint().getX(), 0.1);
    	assertEquals(40f, impact.getPoint().getY(), 0.1);
    	
    	// facing down v
    	ray = new Line2D.Float((float)origin.getX(), (float)origin.getY(), (float)origin.getX(), 600f);    	
    	// 3 horizontal lines at y = 60, 70, 80
    	segment1 = new TrailSegment(0, new Line2D.Float(0f, 60f, 100f, 60f));
    	segment2 = new TrailSegment(0, new Line2D.Float(0f, 70f, 100f, 70f));
    	segment3 = new TrailSegment(0, new Line2D.Float(0f, 80f, 100f, 80f));
    	segments = new ArrayList<TrailSegment>(Arrays.asList(segment1, segment2, segment3));
    	impact = Physics.findClosestImpactPoint(origin, ray, segments);
    	assertNotNull(impact);
    	assertEquals(50f, impact.getPoint().getX(), 0.1);
    	assertEquals(60f, impact.getPoint().getY(), 0.1);
    }
}
