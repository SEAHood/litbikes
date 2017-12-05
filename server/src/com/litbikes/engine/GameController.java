package com.litbikes.engine;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.litbikes.dto.ClientGameJoinDto;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.GameSettingsDto;
import com.litbikes.dto.HelloDto;
import com.litbikes.dto.GameJoinDto;
import com.litbikes.dto.ScoreDto;
import com.litbikes.model.Bike;
import com.litbikes.model.IPlayer;
import com.litbikes.model.Player;

interface GameEventListener {
	void playerCrashed(Bike bike);
	void playerSpawned(int pid);
	void scoreUpdated(List<ScoreDto> scores);
	void gameStarted();
}

// Manages connections between high level game components
public class GameController implements GameEventListener {

	private static Logger LOG = Log.getLogger(GameController.class);
	private final SocketIOServer ioServer;
	private final Map<UUID, IPlayer> sessionPlayers;
	private final GameEngine game;
	private final BotController botController;
	private int pidGen = 0;
	
	private final static String C_HELLO = "hello";
	private final static String C_JOINED_GAME = "joined-game";
	private final static String C_ERROR = "error";
	
	private int minPlayers;
	private Random random = new Random();
		
	public GameController( SocketIOServer _ioServer, int _minPlayers, int gameSize ) {
		ioServer = _ioServer;
		minPlayers = _minPlayers;
		game = new GameEngine(gameSize);
		sessionPlayers = new HashMap<>();
		botController = new BotController(this);
	}
	
	public void start() {
		setupGameListeners();
		game.start();
		balanceBots();
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
		}, 0, 50, TimeUnit.MILLISECONDS);
	}

	public void playerCrashed( Bike bike ) {
		String crashedInto = bike.crashedIntoSelf() ? "their own trail!" : bike.getCrashedInto().getName();
    	String playerCrashedMessage = bike.getName() + " crashed into " + crashedInto;
		LOG.info(playerCrashedMessage);
    	ChatMessageDto dto = new ChatMessageDto(null, null, playerCrashedMessage, true);
    	broadcastData("chat-message", dto);
		broadcastWorldUpdate();
	}
	
	public void playerSpawned( int pid ) {
		broadcastWorldUpdate();
	}

	public void scoreUpdated(List<ScoreDto> scores) {
		broadcastData("score-update", scores);
	}
	// END GAME EVENTS

	
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

	public Bot botCreated() {
		String botName = "BOT#" + String.format("%04d", random.nextInt(10000));
		int pid = pidGen++;
		Bike bike = game.playerJoin(pid, botName);
		Bot bot = new Bot(bike, game.getBikes(), game.getArena());
		sessionPlayers.put(bot.getSessionId(), bot);
		return bot;
	}
	
	public void botDestroyed(Bot bot) {
		game.dropPlayer(bot.getPid());
		sessionPlayers.remove(bot.getSessionId());
	}
	
	private void balanceBots() {
		int totalHumans = (int)sessionPlayers.entrySet().stream()
				.map(p -> p.getValue())
				.filter(p -> p.isHuman() && p.isAlive())
				.count();
		int requiredBots = Math.max(0, minPlayers - totalHumans);
		LOG.info("Balancing game - " + totalHumans + " humans and " + requiredBots + " bots");
		botController.setBotCount(requiredBots);
	}
	
	// CLIENT EVENTS
	public void clientJoiningGame(SocketIOClient client, ClientGameJoinDto gameJoinDto) {
		if (!gameJoinDto.isValid()) {
			// TODO Implement some error handling
			client.sendEvent(C_ERROR, "invalid name");
			return;
		}
		
		Player player = (Player)sessionPlayers.get(client.getSessionId());
		player.name = gameJoinDto.name;
		player.bike = game.playerJoin(player.pid, player.name);		
		
		GameSettingsDto gameSettings = new GameSettingsDto();
		gameSettings.gameTickMs = game.getGameTickMs();
		
		GameJoinDto dto = new GameJoinDto();
		dto.bike = player.bike.getDto();
		dto.scores = game.getScores();
		
		balanceBots();
		
		client.sendEvent(C_JOINED_GAME, dto);
	}
	
	public void clientHello(SocketIOClient client) {
		int pid = pidGen++;
		Player player = new Player(pid);
		
		sessionPlayers.put(client.getSessionId(), player);
		
		GameSettingsDto gameSettings = new GameSettingsDto();
		gameSettings.gameTickMs = game.getGameTickMs();
		
		HelloDto dto = new HelloDto();
		dto.gameSettings = gameSettings;		
		dto.world = game.getWorldDto();
		
		client.sendEvent(C_HELLO, dto);
	}
	
	public void clientDisconnectEvent(SocketIOClient client) {
		try {
        	IPlayer player = sessionPlayers.get(client.getSessionId());           	
        	if ( player == null ) 
        		return; // Client doesn't exist - what should we do here?   	
    		game.dropPlayer(player.getPid());
		} catch (Exception e) {
			
		}
		balanceBots();
	}

	public void clientChatMessageEvent(SocketIOClient client, String message) {
    	LOG.info("Received chat message");

    	IPlayer player = sessionPlayers.get(client.getSessionId());            	
    	if ( player == null ) 
    		return; // Client doesn't exist - what should we do here?
    	 	
    	Color colour = player.getBike().getColour();
    	String sourceColour = String.format("rgba(%s,%s,%s,%%A%%)", colour.getRed(), colour.getGreen(), colour.getBlue()); // TODO Override tostring on Color
    	ChatMessageDto dto = new ChatMessageDto(player.getName(), sourceColour, message, false);

    	broadcastData("chat-message", dto);		
	}

	public void clientUpdateEvent(SocketIOClient client, ClientUpdateDto updateDto) {
    	IPlayer player = sessionPlayers.get(client.getSessionId());
    	if ( player == null ) 
    		return; // Client doesn't exist - what should we do here?
		
    	if ( game.handleClientUpdate(updateDto) ) {
    		broadcastWorldUpdate();
    	}		
	}

	public void clientRequestRespawnEvent(SocketIOClient client) {
    	IPlayer player = sessionPlayers.get(client.getSessionId());      	
    	if ( player == null ) 
    		return; // Client doesn't exist - what should we do here?
    	
		LOG.info("Respawn request from " + player.getName());
		game.requestRespawn(player.getPid());
		
	}

	public void clientHelloEvent(SocketIOClient client) {
    	LOG.info("Received hello");
    	clientHello(client);
	}
	
	public void clientRequestWorldEvent(SocketIOClient client) {
    	sendWorldUpdate(client);
	}

	public void clientKeepAliveEvent(SocketIOClient client) {
		// todo time out client after 2 missed keep alives or something?
		client.sendEvent("keep-alive-ack");
	}

	public void clientRequestGameJoinEvent(SocketIOClient client, ClientGameJoinDto gameJoinDto) {
    	LOG.info("Received game join request event");
    	clientJoiningGame(client, gameJoinDto);
    	
    	// TODO Make sure client isn't trying to rejoin - i.e. if they already have a name
    	
    	String newPlayerMessage = gameJoinDto.name + " joined!";
    	ChatMessageDto dto = new ChatMessageDto(null, null, newPlayerMessage, true);
    	broadcastData("chat-message", dto);
	}
	
	// END CLIENT EVENTS
	
}
