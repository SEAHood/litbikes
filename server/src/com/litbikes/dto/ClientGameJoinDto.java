package com.litbikes.dto;

public class ClientGameJoinDto {
	public String name;
	
	public boolean isValid() {
		return name.length() > 1 || name.length() <= 15;
	}
}