package com.litbikes.dto;

import com.litbikes.util.Vector;

//Update DTO from client
public class ClientUpdateDto {
	public Integer pid;
	public Integer xpos;
	public Integer ypos;
	public Integer xspd;
	public Integer yspd;
	public Boolean dead;

	public Vector getPos() {
		if ( xpos != null && ypos != null )
			return new Vector(xpos, ypos);
		else
			return null;
	}
	
	public Vector getSpd() {
		if ( xspd != null && yspd != null )
			return new Vector(xspd, yspd);
		else
			return null;
	}
	
	public boolean isValid() {
		return pid != null && 
			   xpos != null && 
			   ypos != null && 
			   xspd != null && 
			   yspd != null;
	}
}
