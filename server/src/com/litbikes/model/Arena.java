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
}
