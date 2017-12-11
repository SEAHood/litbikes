package com.litbikes.model;

public abstract class PowerUp {
	private String name;
	
	public PowerUp(String _name) {
		setName(_name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public abstract void activate();
}
