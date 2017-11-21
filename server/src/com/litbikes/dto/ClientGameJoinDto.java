package com.litbikes.dto;

public class ClientGameJoinDto {
	public String name;
	
	public boolean isValid() {
		if (name.length() < 2 || name.length() > 10)
			return false;
		return true;
	}
}
