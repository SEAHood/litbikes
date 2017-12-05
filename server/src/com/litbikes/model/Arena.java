package com.litbikes.model;

import com.litbikes.dto.ArenaDto;
import com.litbikes.util.Vector;

public class Arena {
	public int size;
	
	public Arena( int gameSize ) {
		size = gameSize;
	}
	
	public ArenaDto getDto() {
		ArenaDto dto = new ArenaDto();
		dto.size = this.size;
		return dto;
	}
	
	public boolean checkCollision( Bike bike, int lookAhead ) {		
		Vector bPos = bike.getPos();
		double collisionX = bPos.x + (lookAhead * bike.getDir().x);
		double collisionY = bPos.y + (lookAhead * bike.getDir().y);
		return collisionX >= size || collisionX <= 0 || collisionY >= size || collisionY <= 0;
	}
}
