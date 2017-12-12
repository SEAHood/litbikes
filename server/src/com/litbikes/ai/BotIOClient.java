package com.litbikes.ai;

import java.net.SocketAddress;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.protocol.Packet;
import com.litbikes.model.Arena;
import com.litbikes.model.Player;

// TODO : Could probably extend this to make the bots completely separate from the game controller
// Override client send() implementation to send data to bot instead 
public class BotIOClient implements SocketIOClient {

	private final UUID sessionId;
	private final Bot bot;
	
	public BotIOClient( Bot _bot ) {
		sessionId = UUID.randomUUID();
		bot = _bot;
	}
	
	@Override
	public UUID getSessionId() {
		return sessionId;
	}	

	public void updateBot(List<Player> bikes, Arena arena) {
		bot.updateWorld(bikes, arena);
	}
	
	@Override
	public void sendEvent( String event, Object... data ) {
	}
	
	// SocketIOClient required overrides
	@Override public void send(Packet packet) {}
	@Override public void disconnect() {}
	@Override public void set(String key, Object val) {}
	@Override public <T> T get(String key) { return null; }
	@Override public boolean has(String key) { return false; }
	@Override public void del(String key) {}
	@Override public HandshakeData getHandshakeData() { return null; }
	@Override public Transport getTransport() { return null; }
	@Override public void sendEvent(String name, AckCallback<?> ackCallback, Object... data) {}
	@Override public void send(Packet packet, AckCallback<?> ackCallback) {}
	@Override public SocketIONamespace getNamespace() { return null; }
	@Override public SocketAddress getRemoteAddress() { return null; }
	@Override public boolean isChannelOpen() { return false; }
	@Override public void joinRoom(String room) {}
	@Override public void leaveRoom(String room) {}
	@Override public Set<String> getAllRooms() { return null; }
	
}
