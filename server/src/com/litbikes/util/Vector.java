package com.litbikes.util;

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
}
