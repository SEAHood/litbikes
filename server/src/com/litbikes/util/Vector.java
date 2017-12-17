package com.litbikes.util;

import java.util.Random;

public class Vector {

	public double x;
	public double y;
	
	// Why not eh
	public Vector(int x, int y) {
		this((double)x, (double)y);
	}
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void add( Vector that ) {
		this.x += that.x;
		this.y += that.y;
	}
	
	public static Vector zero() {
		return new Vector(0, 0);
	}
	
	public static Vector random(int maxX, int maxY) {
		return new Vector(new Random().nextInt(maxX), new Random().nextInt(maxX));
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
