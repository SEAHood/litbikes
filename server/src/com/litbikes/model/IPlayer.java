package com.litbikes.model;

public interface IPlayer {
	public abstract int getPid();
	public abstract String getName();
	public abstract Bike getBike();
	public abstract boolean isAlive();
	public abstract boolean isHuman();
}
