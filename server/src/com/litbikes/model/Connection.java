package com.litbikes.model;

import java.util.UUID;

// Ties a session ID to a specific bike
public class Connection {
	private int pid;
	private UUID sessionId;
	
	public Connection(int pid, UUID sessionId) {
		this.pid = pid;
		this.sessionId = sessionId;
	}
	
	public int getPid() {
		return pid;
	}
	public UUID getSessionId() {
		return sessionId;
	}	
}
