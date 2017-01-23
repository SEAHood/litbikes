package com.litbikes.dto;

import java.util.List;

import com.litbikes.util.Vector;

public class BikeDto {
	public int pid;
	public Vector pos;
	public Vector spd;
	public double spdMag;
	public List<Vector> trail;
	public boolean crashed;
	public boolean spectating;
}
