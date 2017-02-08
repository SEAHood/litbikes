package com.litbikes.model;

import com.litbikes.dto.ArenaDto;
import com.litbikes.util.Vector;

public class Arena {
	public Vector dimensions;
	
	public Arena( Vector dimensions ) {
		this.dimensions = dimensions;
	}
	
	public ArenaDto getDto() {
		ArenaDto dto = new ArenaDto();
		dto.dimensions = this.dimensions;
		return dto;
	}
	
	public boolean checkCollision( Bike bike, int lookAhead ) {		
		Vector bPos = bike.getPos();
		double collisionX = bPos.x + (lookAhead * bike.getSpd().x);
		double collisionY = bPos.y + (lookAhead * bike.getSpd().y);
		return collisionX >= dimensions.x || collisionX <= 0 || collisionY >= dimensions.y || collisionY <= 0;
	}
}
