package com.litbikes.server;

import java.util.ArrayList;
import java.util.List;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.game.Game;
import com.litbikes.model.Connection;

public class GameServer {

	private final static String REGISTER = "register";
	private final static String CLIENT_REGISTERED = "client-registered";
	private final static String CLIENT_UPDATE = "client-update";
	private final static String REQUEST_WORLD_UPDATE = "request-world-update";
	
	private static Game game;
	private static SocketIOServer ioServer;
	private static List<Connection> connections;
	
	
	public static void main(String[] args) throws InterruptedException {

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        
        ioServer = new SocketIOServer(config);
    	game = Game.create();
    	connections = new ArrayList<>();
        
        setupSocketListeners();
        
        ioServer.start();      
    	game.start();

    	System.out.println("Gameserver started!");
        //Thread.sleep(Integer.MAX_VALUE);

        //server.stop();
	}
	
	public static void setupSocketListeners() {
		ioServer.addEventListener(REGISTER, String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	System.out.println("Received register event");
            	registerClient(client);
            }
        });

        ioServer.addEventListener(REQUEST_WORLD_UPDATE, String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	sendWorldUpdate(client);
            }
        });

        ioServer.addEventListener(CLIENT_UPDATE, ClientUpdateDto.class, new DataListener<ClientUpdateDto>() {
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
        		Connection clientConnection = connections.stream().filter(c -> c.getSessionId() == client.getSessionId()).findFirst().get();
        		game.dropPlayer(clientConnection.getPid());
        		//client.getSessionId();
        	}
        });
	}
	
	public static void registerClient(SocketIOClient client) {
		int newPid = game.newPlayer();
		connections.add( new Connection(newPid, client.getSessionId()) );
		client.sendEvent(CLIENT_REGISTERED, newPid);
	}
	
	public static void sendWorldUpdate(SocketIOClient client) {
		client.sendEvent("world-update", game.getWorldDto());
	}
	
	public static void broadcastWorldUpdate() {
		ioServer.getBroadcastOperations().sendEvent("world-update", game.getWorldDto());
	}
	
	public static void broadcastData(String key, Object obj) {
		ioServer.getBroadcastOperations().sendEvent(key, obj);
	}
        
}
