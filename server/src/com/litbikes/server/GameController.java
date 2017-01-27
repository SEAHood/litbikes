package com.litbikes.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.GameSettingsDto;
import com.litbikes.dto.RegistrationDto;
import com.litbikes.model.Bike;
import com.litbikes.model.Connection;

interface GameEventListener {
	void playerCrashed(int pid);
	void playerSpawned(int pid);
	void gameStarted();
}

interface BotListener {
	void requestedWorldUpdate(Bot bot);
	void sentClientUpdate(ClientUpdateDto updateDto);
	void sentRequestRespawn(Bot bot);
}

// Orchestrates events between the game and the io server
// Manages connections and bots
public class GameController implements GameEventListener, BotListener {

	Logger log = Log.getLog();
	private SocketIOServer ioServer;
	private List<Connection> connections;
	private List<Bot> bots;
	private Game game;

	private final static String C_REGISTER = "register";
	private final static String C_REGISTERED = "registered";
	private final static String C_UPDATE = "update";
	private final static String C_REQUEST_WORLD = "request-world";
	private final static String C_REQUEST_RESPAWN = "request-respawn";
	
	private class ClientEventListener<T> implements DataListener<T> {		
		public void onData(SocketIOClient client, T data, AckRequest ackRequest) {
			//addLatency();
		}
		
		/*void addLatency() {
        	try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/		
	}
	
	public GameController( SocketIOServer ioServer, Game game ) {
		this.ioServer = ioServer;
		this.game = game;
		connections = new ArrayList<>();
		bots = new ArrayList<>();	
	}
	
	public void initialise() {
		setupSocketListeners();
		setupGameListeners();
	}
	
	// Incoming socket messages
	public void setupSocketListeners() {
		ioServer.addEventListener(C_REGISTER, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	System.out.println("Received register event");
            	registerClient(client);
            }
        });

        ioServer.addEventListener(C_REQUEST_WORLD, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	sendWorldUpdate(client);
            }
        });

        ioServer.addEventListener(C_REQUEST_RESPAWN, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
        		Connection clientConnection = connections.stream().filter(c -> c.getSessionId() == client.getSessionId()).findFirst().get();
        		System.out.println("Respawn request from " + clientConnection.getPid());
        		game.requestRespawn(clientConnection.getPid());
            }
        });

        ioServer.addEventListener(C_UPDATE, ClientUpdateDto.class, new ClientEventListener<ClientUpdateDto>() {
            @Override
            public void onData(final SocketIOClient client, ClientUpdateDto updateDto, final AckRequest ackRequest) {
            	super.onData(client, updateDto, ackRequest);
            	handleClientUpdateEvent(updateDto);
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
        	}
        });
	}
	
	private void handleClientUpdateEvent(ClientUpdateDto updateDto) {
    	if ( game.handleClientUpdate(updateDto) ) {
    		broadcastWorldUpdate();
    	}
	}
	
	// START GAME EVENTS
	private void setupGameListeners() {
		game.attachListener(this);		
	}	
	
	public void gameStarted() {		
		int botCount = 5;
		for ( int i = 0; i < botCount; i++ ) {
			Bot bot = addBot();
			bot.start();
		}
	}

	public void playerCrashed( int pid ) {
		broadcastWorldUpdate();
	}
	
	public void playerSpawned( int pid ) {
		broadcastWorldUpdate();
	}
	// END GAME EVENTS
	
	public void registerClient( SocketIOClient client ) {
		Bike bike = game.newPlayer();
		connections.add( new Connection(bike.getPid(), client.getSessionId()) );
		
		GameSettingsDto gameSettings = new GameSettingsDto();
		gameSettings.gameTickMs = game.getGameTickMs();
		
		RegistrationDto dto = new RegistrationDto();
		dto.bike = bike.getDto();
		dto.gameSettings = gameSettings;		
		dto.world = game.getWorldDto();
		
		client.sendEvent(C_REGISTERED, dto);
	}
	
	public void sendWorldUpdate(SocketIOClient client) {
		client.sendEvent("world-update", game.getWorldDto());
	}
	
	public void broadcastWorldUpdate() {
		ioServer.getBroadcastOperations().sendEvent("world-update", game.getWorldDto());
		botWorldUpdate();
	}
	
	public void broadcastData(String key, Object obj) {
		ioServer.getBroadcastOperations().sendEvent(key, obj);
	}
	
	// Bots
	private Bot addBot() {
		Bike bike = game.newPlayer();
		Bot bot = new Bot(bike, game.getBikes(), game.getArena());
		bot.attachListener(this);
		bots.add(bot);
		log.info("Creating new bot with pid " + bike.getPid());
		return bot;
	}

	private void botWorldUpdate() {
		for ( Bot bot : bots ) {
			bot.updateWorld(game.getBikes(), game.getArena());
		}
	}

	public void requestedWorldUpdate( Bot bot ) {
		bot.updateWorld(game.getBikes(), game.getArena());
	}
	
	public void sentClientUpdate( ClientUpdateDto updateDto ) {
		handleClientUpdateEvent(updateDto);
	}
	
	public void sentRequestRespawn( Bot bot ) {
		game.requestRespawn(bot.getPid());
	}
	// Bots end
	
}
