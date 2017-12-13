package com.litbikes.dto;

import java.util.List;

import com.litbikes.util.Vector;

public class BikeDto {
	public Vector pos;
	public Vector dir;
	public double spd;
	public List<TrailSegmentDto> trail;
	public String colour; // in rgba(0,0,0,%A%) format
}
