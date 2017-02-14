package com.litbikes.server;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.litbikes.dto.ChatMessageDto;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.GameSettingsDto;
import com.litbikes.dto.RegistrationDto;
import com.litbikes.model.Bike;

interface GameEventListener {
	void playerCrashed(Bike bike);
	void playerSpawned(int pid);
	void gameStarted();
}

interface BotListener {
	void requestedWorldUpdate(Bot bot);
	void sentClientUpdate(BotIOClient client, ClientUpdateDto updateDto);
	void sentRequestRespawn(Bot bot);
}

// Orchestrates events between the game and the io server
// Manages connections and bots
public class GameController implements GameEventListener, BotListener {

	private static Logger LOG = Log.getLogger(GameController.class);
	private final SocketIOServer ioServer;
	//private List<Connection> connections;
	private final Map<UUID, Integer> sessionPids;
	private List<Bot> bots;
	private final Game game;

	private final static String C_REGISTER = "register";
	private final static String C_REGISTERED = "registered";
	private final static String C_UPDATE = "update";
	private final static String C_REQUEST_WORLD = "request-world";
	private final static String C_REQUEST_RESPAWN = "request-respawn";
	private final static String C_KEEP_ALIVE = "keep-alive";
	private final static String C_CHAT_MESSAGE = "chat-message";
	
	private class ClientEventListener<T> implements DataListener<T> {		
		public void onData(SocketIOClient client, T data, AckRequest ackRequest) {
			//addLatency();
		}
		
		@SuppressWarnings("unused")
		void addLatency() {
        	try { Thread.sleep(30); } catch (InterruptedException e) {}
		}
	}
	
	public GameController( SocketIOServer _ioServer, Game _game ) {
		ioServer = _ioServer;
		game = _game;
		bots = new ArrayList<>();
		sessionPids = new HashMap<>();
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
            	LOG.info("Received register event");
            	registerClient(client);
            	
            	String newPlayerMessage = "Player " + sessionPids.get(client.getSessionId()) + " joined!";
            	ChatMessageDto dto = new ChatMessageDto(null, null, newPlayerMessage, true);
            	broadcastData("chat-message", dto);
            }
        });

		ioServer.addEventListener(C_KEEP_ALIVE, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	// TODO Figure out how ackRequest works
        		client.sendEvent("keep-alive-ack");
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
            	
            	Integer clientPid = sessionPids.get(client.getSessionId());            	
            	if ( clientPid == null ) 
            		return; // Client doesn't exist - what should we do here?
            	
        		LOG.info("Respawn request from " + clientPid);
        		game.requestRespawn(clientPid);
            }
        });

        ioServer.addEventListener(C_UPDATE, ClientUpdateDto.class, new ClientEventListener<ClientUpdateDto>() {
            @Override
            public void onData(final SocketIOClient client, ClientUpdateDto updateDto, final AckRequest ackRequest) {
            	super.onData(client, updateDto, ackRequest);
            	handleClientUpdateEvent(client, updateDto);
            }
        });

        ioServer.addEventListener(C_CHAT_MESSAGE, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String message, final AckRequest ackRequest) {
            	super.onData(client, message, ackRequest);
            	LOG.info("Received chat message");
            	handleChatMessageEvent(client, message);
            }
        });
        
        ioServer.addDisconnectListener(new DisconnectListener() {
        	@Override
        	public void onDisconnect(final SocketIOClient client) {
        		try {
                	Integer clientPid = sessionPids.get(client.getSessionId());            	
                	if ( clientPid == null ) 
                		return; // Client doesn't exist - what should we do here?
                	
            		game.dropPlayer(clientPid);
        		} catch (Exception e) {
        			
        		}
        	}
        });
	}
	
	private void handleClientUpdateEvent( SocketIOClient client, ClientUpdateDto updateDto ) {
		if ( client == null ) {
			
		}
		
    	Integer clientPid = sessionPids.get(client.getSessionId());            	
    	if ( clientPid == null ) 
    		return; // Client doesn't exist - what should we do here?
		
		long startTime = System.nanoTime();
    	if ( game.handleClientUpdate(updateDto) ) {
    		long endTime = System.nanoTime();    		
    		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
    		LOG.info( "Handled client update in " + duration + " nanoseconds" );
    		broadcastWorldUpdate();
    	}
	}
	
	private void handleChatMessageEvent( SocketIOClient client, String message ) {
    	Integer clientPid = sessionPids.get(client.getSessionId());            	
    	if ( clientPid == null ) 
    		return; // Client doesn't exist - what should we do here?
    	
    	Color colour = game.getBikes().stream().filter(b -> b.getPid() == clientPid).findFirst().get().getColour();
    	String sourceColour = String.format("rgba(%s,%s,%s,%%A%%)", colour.getRed(), colour.getGreen(), colour.getBlue()); // TODO Override tostring on Color
    	ChatMessageDto dto = new ChatMessageDto(clientPid.toString(), sourceColour, message, false);

    	broadcastData("chat-message", dto);
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

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				broadcastWorldUpdate();
			}
		}, 0, 1500, TimeUnit.MILLISECONDS);
	}

	public void playerCrashed( Bike bike ) {
		LOG.info("Bike " + bike.getPid() + " crashed into " + bike.getCrashedInto());
		broadcastWorldUpdate();
	}
	
	public void playerSpawned( int pid ) {
		broadcastWorldUpdate();
	}
	// END GAME EVENTS
	
	public void registerClient( SocketIOClient client ) {
		Bike bike = game.newPlayer();
		
		sessionPids.put(client.getSessionId(), bike.getPid());
		
		GameSettingsDto gameSettings = new GameSettingsDto();
		gameSettings.gameTickMs = game.getGameTickMs();
		
		RegistrationDto dto = new RegistrationDto();
		dto.bike = bike.getDto();
		dto.gameSettings = gameSettings;		
		dto.world = game.getWorldDto();
		
		client.sendEvent(C_REGISTERED, dto);
	}
	
	public void sendWorldUpdate(SocketIOClient client) {
		if ( client != null )
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
		sessionPids.put(bot.getSessionId(), bot.getPid());
		LOG.info("Creating new bot with pid " + bike.getPid());
		return bot;
	}

	private void botWorldUpdate() {
		for ( Bot bot : bots ) {
			bot.updateWorld(game.getBikes(), game.getArena());
		}
	}

	// TODO : Put these events through an event system that deals with SocketIOClient - see BotIOClient
	public void requestedWorldUpdate( Bot bot ) {
		bot.updateWorld(game.getBikes(), game.getArena());
	}

	// TODO : Put these events through an event system that deals with SocketIOClient - see BotIOClient
	public void sentClientUpdate( BotIOClient client, ClientUpdateDto updateDto ) {
		handleClientUpdateEvent(client, updateDto);
	}

	// TODO : Put these events through an event system that deals with SocketIOClient - see BotIOClient
	public void sentRequestRespawn( Bot bot ) {
		game.requestRespawn(bot.getPid());
	}
	// Bots end
	
}
