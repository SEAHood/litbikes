package com.litbikes.dto;

import java.util.List;

import com.litbikes.util.Vector;

public class BikeDto {
	public int pid;
	public String name;
	public Vector pos;
	public Vector spd;
	public double spdMag;
	public List<TrailSegmentDto> trail;
	public boolean crashed;
	public Integer crashedInto;
	public String crashedIntoName;
	public boolean spectating;
	public String colour; // in rgba(0,0,0,%A%) format
	public int score;
}
