package com.litbikes.model;

import com.litbikes.dto.PlayerDto;
import com.litbikes.model.PowerUp.PowerUpType;

public class Player implements ICollidable {
	public enum PlayerEffect {
		NONE,
		SLOWED
	}

	protected int pid;
	protected Bike bike;
	private String name;
	private boolean crashed = false;
	private boolean spectating = false;
	private ICollidable crashedInto = null;
	private boolean isHuman;
	private PowerUpType currentPowerUpType = null;
	private PlayerEffect effect = PlayerEffect.NONE;
	
	public Player(int _pid, boolean _isHuman) {
		pid = _pid;
		isHuman = _isHuman;
	}
	
	public int getId() {
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
		return !crashed && !spectating;
	}
	
	public boolean isHuman() {
		return isHuman;
	}
	
	public PowerUpType getCurrentPowerUpType() {
		return currentPowerUpType;
	}
	
	public void setCurrentPowerUpType(PowerUpType type) {
		currentPowerUpType = type;
	}
	
	public PlayerEffect getEffect() {
		return effect;
	}
	
	public void setEffect(PlayerEffect effect) {
		this.effect = effect;
	}
	
	public PlayerDto getDto() {
		PlayerDto dto = new PlayerDto();
		dto.pid = pid;
		dto.name = name;
		dto.bike = bike.getDto();
		dto.crashed = crashed;
		dto.crashedInto = crashedInto != null ? crashedInto.getId() : null;
		dto.crashedIntoName = crashedInto != null ? crashedInto.getName() : null;
		dto.spectating = spectating;
		dto.currentPowerUp = currentPowerUpType;
		dto.effect = effect;
		return dto;
	}
	
	public void update() {
		if (isAlive()) {
			bike.updatePosition();	
		}
	}

	public void updateBike(Bike _bike) {
		// this is terrible
		bike.setPos(_bike.getPos());
		bike.setDir(_bike.getDir());
	}
	
	public boolean crashedIntoSelf() {
		if (crashedInto == null)
			return false;
		return crashedInto.getId() == pid;
	}	
	
	public boolean isSpectating() {
		return spectating;
	}

	public void setSpectating(boolean spectating) {
		this.spectating = spectating;
	}

	public ICollidable getCrashedInto() {
		return crashedInto;
	}

	public void setCrashedInto(ICollidable crashedInto) {
		this.crashedInto = crashedInto;
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public void crashed(ICollidable collidedWith) {
		bike.crash();
		setCrashed(true);
		setCrashedInto(collidedWith);
		setSpectating(true);
	}
	
	public void respawn(Spawn spawn) {
		bike.init(spawn, false);
		setCrashed(false);
		setCrashedInto(null);
		setSpectating(false);
		effect = PlayerEffect.NONE;
	}
}
