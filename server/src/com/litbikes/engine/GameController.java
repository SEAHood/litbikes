package com.litbikes.engine;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.litbikes.ai.Bot;
import com.litbikes.ai.BotController;
import com.litbikes.ai.BotIOClient;
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

// Manages connections between high level game components
public class GameController implements GameEventListener {

	private static Logger LOG = Log.getLogger(GameController.class);
	private final SocketIOServer ioServer;
	private final Map<UUID, Integer> sessionPids;
	private final GameEngine game;
	private final BotController botController;
	
	private final static String C_REGISTERED = "registered";
	
	private int maxBots;
		
	public GameController( SocketIOServer _ioServer, int _maxBots, int gameWidth, int gameHeight ) {
		ioServer = _ioServer;
		maxBots = _maxBots;
		game = new GameEngine(gameWidth, gameHeight);
		sessionPids = new HashMap<>();
		botController = new BotController(this);
	}
	
	public void start() {
		setupGameListeners();
		game.start();
		botController.deployBots(maxBots);
	}

	
	// START GAME EVENTS
	private void setupGameListeners() {
		game.attachListener(this);
	}	
	
	public void gameStarted() {		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				broadcastWorldUpdate();
			}
		}, 0, 5, TimeUnit.SECONDS);
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
		if ( client != null ) {
			if ( client instanceof BotIOClient ) 
				((BotIOClient)client).updateBot(game.getBikes(), game.getArena());
			else
				client.sendEvent("world-update", game.getWorldDto());
		}
	}
	
	public void broadcastWorldUpdate() {
		ioServer.getBroadcastOperations().sendEvent("world-update", game.getWorldDto());
		botController.doUpdate(game.getBikes(), game.getArena());
	}
	
	public void broadcastData(String key, Object obj) {
		ioServer.getBroadcastOperations().sendEvent(key, obj);
	}

	public Bot createBot() {
		Bike bike = game.newPlayer();
		Bot bot = new Bot(bike, game.getBikes(), game.getArena());
		sessionPids.put(bot.getSessionId(), bot.getPid());
		return bot;
	}

	public void clientDisconnectEvent(SocketIOClient client) {
		try {
        	Integer clientPid = sessionPids.get(client.getSessionId());            	
        	if ( clientPid == null ) 
        		return; // Client doesn't exist - what should we do here?
        	
    		game.dropPlayer(clientPid);
		} catch (Exception e) {
			
		}
	}

	public void clientChatMessageEvent(SocketIOClient client, String message) {
    	LOG.info("Received chat message");

    	Integer clientPid = sessionPids.get(client.getSessionId());            	
    	if ( clientPid == null ) 
    		return; // Client doesn't exist - what should we do here?
    	
    	Color colour = game.getBikes().stream().filter(b -> b.getPid() == clientPid).findFirst().get().getColour();
    	String sourceColour = String.format("rgba(%s,%s,%s,%%A%%)", colour.getRed(), colour.getGreen(), colour.getBlue()); // TODO Override tostring on Color
    	ChatMessageDto dto = new ChatMessageDto(clientPid.toString(), sourceColour, message, false);

    	broadcastData("chat-message", dto);		
	}

	public void clientUpdateEvent(SocketIOClient client, ClientUpdateDto updateDto) {
    	Integer clientPid = sessionPids.get(client.getSessionId());     
    	if ( clientPid == null ) 
    		return; // Client doesn't exist - what should we do here?
		
    	if ( game.handleClientUpdate(updateDto) ) {
    		broadcastWorldUpdate();
    	}		
	}

	public void clientRequestRespawnEvent(SocketIOClient client) {
    	Integer clientPid = sessionPids.get(client.getSessionId());            	
    	if ( clientPid == null ) 
    		return; // Client doesn't exist - what should we do here?
    	
		LOG.info("Respawn request from " + clientPid);
		game.requestRespawn(clientPid);
		
	}

	public void clientRequestWorldEvent(SocketIOClient client) {
    	sendWorldUpdate(client);
	}

	public void clientKeepAliveEvent(SocketIOClient client) {
		client.sendEvent("keep-alive-ack");
	}

	public void clientRegisterEvent(SocketIOClient client) {
    	LOG.info("Received register event");
    	registerClient(client);
    	
    	String newPlayerMessage = "Player " + sessionPids.get(client.getSessionId()) + " joined!";
    	ChatMessageDto dto = new ChatMessageDto(null, null, newPlayerMessage, true);
    	broadcastData("chat-message", dto);
	}
	
}
