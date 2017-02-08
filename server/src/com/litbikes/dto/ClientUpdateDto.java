package com.litbikes.dto;

import com.litbikes.util.Vector;

//Update DTO from client
public class ClientUpdateDto {
	public Integer pid;
	public Integer xSpd;
	public Integer ySpd;
	public Integer xPos;
	public Integer yPos;

	public Vector getSpd() {
		if ( xSpd != null && ySpd != null )
			return new Vector(xSpd, ySpd);
		else
			return null;
	}
	
	public boolean isValid() {
		return pid != null && 
			   xSpd != null && xSpd <= 1 && xSpd >= -1 &&
			   ySpd != null && ySpd <= 1 && ySpd >= -1;
	}
}
