package com.litbikes.server;

import java.util.ArrayList;
import java.util.List;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.litbikes.dto.BikeDto;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.model.Connection;

interface GameEventListener {
	void playerCrashed(int pid);
	void playerSpawned(int pid);
}

// Orchestrates events between the game and the io server
public class EventLayer implements GameEventListener {

	private SocketIOServer ioServer;
	private List<Connection> connections;
	private Game game;

	private final static String C_REGISTER = "register";
	private final static String C_REGISTERED = "registered";
	private final static String C_UPDATE = "update";
	private final static String C_REQUEST_WORLD = "request-world";
	private final static String C_REQUEST_RESPAWN = "request-respawn";
	
	public EventLayer( SocketIOServer ioServer, Game game ) {
		this.ioServer = ioServer;
		this.game = game;
		connections = new ArrayList<>();
		
	}
	
	public void initialise() {
		setupSocketListeners();
		setupGameListeners();
	}
	
	// Incoming socket messages
	public void setupSocketListeners() {
		ioServer.addEventListener(C_REGISTER, String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	System.out.println("Received register event");
            	registerClient(client);
            }
        });

        ioServer.addEventListener(C_REQUEST_WORLD, String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	sendWorldUpdate(client);
            }
        });

        ioServer.addEventListener(C_REQUEST_RESPAWN, String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
        		Connection clientConnection = connections.stream().filter(c -> c.getSessionId() == client.getSessionId()).findFirst().get();
        		System.out.println("Respawn request from " + clientConnection.getPid());
        		game.requestRespawn(clientConnection.getPid());
            }
        });

        ioServer.addEventListener(C_UPDATE, ClientUpdateDto.class, new DataListener<ClientUpdateDto>() {
            @Override
            public void onData(final SocketIOClient client, ClientUpdateDto data, final AckRequest ackRequest) {
            	if ( game.handleClientUpdate(data) ) {
            		broadcastWorldUpdate();
            	}
            }
        });
        
        ioServer.addDisconnectListener(new DisconnectListener() {
        	@Override
        	public void onDisconnect(final SocketIOClient client) {
        		try {
        			//todo : exception here?
            		Connection clientConnection = connections.stream().filter(c -> c.getSessionId() == client.getSessionId()).findFirst().get();
            		game.dropPlayer(clientConnection.getPid());
        		} catch (Exception e) {
        			
        		}
        		//client.getSessionId();
        	}
        });
	}
	
	// START GAME EVENTS
	private void setupGameListeners() {
		game.attachListener(this);		
	}	

	public void playerCrashed( int pid ) {
		System.out.println("Player crashed!");
		broadcastWorldUpdate();
	}
	
	public void playerSpawned( int pid ) {
		System.out.println("Player respawned!");
		broadcastWorldUpdate();
	}
	// END GAME EVENTS
	
	public void registerClient(SocketIOClient client) {
		BikeDto dto = game.newPlayer();
		connections.add( new Connection(dto.pid, client.getSessionId()) );
		client.sendEvent(C_REGISTERED, dto);
	}
	
	public void sendWorldUpdate(SocketIOClient client) {
		client.sendEvent("world-update", game.getWorldDto());
	}
	
	public void broadcastWorldUpdate() {
		ioServer.getBroadcastOperations().sendEvent("world-update", game.getWorldDto());
	}
	
	public void broadcastData(String key, Object obj) {
		ioServer.getBroadcastOperations().sendEvent(key, obj);
	}
	
	
}
