package com.litbikes.model;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.UUID;

import com.litbikes.dto.PowerUpDto;
import com.litbikes.util.Vector;

public class PowerUp {
	public enum PowerUpType {
		NOTHING,
		ROCKET,
		SLOW
	}
	
	private String id;
	private Vector pos;
	private PowerUpType type;
	private boolean collected;
	
	public PowerUp(Vector _pos, PowerUpType _type) {
		id = UUID.randomUUID().toString();
		pos = _pos;
		type = _type;
		collected = false;
	}

	public String getId() {
		return id;
	}

	public Vector getPos() {
		return pos;
	}

	public void setPos(Vector pos) {
		this.pos = pos;
	}

	public PowerUpType getType() {
		return type;
	}

	public void setType(PowerUpType type) {
		this.type = type;
	}

	public boolean isCollected() {
		return collected;		
	}

	public void setCollected(boolean collected) {
		this.collected = collected;
	}
	
	public boolean collides(Line2D line) {
		int boxSize = 6;
		int originX = (int)pos.x - (boxSize / 2);
		int originY = (int)pos.y - (boxSize / 2);
		Rectangle hitbox = new Rectangle(originX, originY, boxSize, boxSize);
		boolean hits = line.intersects(hitbox);
		return line.intersects(hitbox);
	}
	
	public PowerUpDto getDto() {
		PowerUpDto dto = new PowerUpDto();
		dto.id = id;
		dto.pos = pos;
		dto.type = type;
		dto.collected = collected;
		return dto;
	}
}
