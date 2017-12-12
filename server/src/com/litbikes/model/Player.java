package com.litbikes.model;

import com.litbikes.dto.PlayerDto;

public class Player {
	protected int pid;
	protected Bike bike;
	private String name;
	
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
	
	public void setBike(Bike bike) {
		this.bike = bike;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAlive() {
		return bike != null && bike.isActive();
	}
	
	public void respawn(Spawn spawn) {
		bike.init(spawn, false);
	}
	
	public boolean isHuman() {
		return true;
	}
	
	public PlayerDto getDto() {
		PlayerDto dto = new PlayerDto();
		dto.bike = bike.getDto();
		return dto;
	}

	public void updateBike(Bike _bike) {
		// this is terrible
		bike.setPos(_bike.getPos());
		bike.setDir(_bike.getDir());
		bike.setCrashedInto(_bike.getCrashedInto());
	}
	
	
}
