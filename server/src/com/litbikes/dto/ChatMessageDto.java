package com.litbikes.dto;

import java.util.Date;

public class ChatMessageDto {
	public long timestamp;
	public String source;
	public String sourceColour; // in rgba(0,0,0,0) format
	public String message;
	public boolean isSystemMessage;
	
	public ChatMessageDto(String _source, String _sourceColour, String _message, boolean _isSystemMessage) {
		timestamp = new Date().getTime();
		source = _source;
		sourceColour = _sourceColour;
		message = _message;
		isSystemMessage = _isSystemMessage;
	}
}
