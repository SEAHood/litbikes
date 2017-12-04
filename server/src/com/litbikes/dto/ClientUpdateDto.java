package com.litbikes.dto;

import com.litbikes.util.Vector;

//Update DTO from client
public class ClientUpdateDto {
	public Integer pid;
	public Integer xDir;
	public Integer yDir;
	public Integer xPos;
	public Integer yPos;

	public Vector getDir() {
		if ( xDir != null && yDir != null )
			return new Vector(xDir, yDir);
		else
			return null;
	}
	
	public boolean isValid() {
		return pid != null && 
			   xDir != null && xDir <= 1 && xDir >= -1 &&
			   yDir != null && yDir <= 1 && yDir >= -1;
	}
}
