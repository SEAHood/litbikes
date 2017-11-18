package com.litbikes.model;

public class Player implements IPlayer {
	public int pid;
	public Bike bike;
	public String name;
	
	public Player(int _pid) {
		pid = _pid;
	}
	
	public int getPid() {
		return pid;
	}
	
	public String getName() {
		return name;
	}

	public Bike getBike() {
		return bike;
	}
	
	public boolean isAlive() {
		return bike.isActive();
	}
	
	public boolean isHuman() {
		return true;
	}
}
