package com.litbikes.server;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.litbikes.dto.ClientGameJoinDto;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.engine.GameController;

public class GameServer {
	
	private static Logger LOG = Log.getLogger(GameServer.class);
	private SocketIOServer ioServer;	
	private Configuration config;
	private GameController gameController;
	
	private final int LATENCY_THROTTLE = 60;

	private final static String C_HELLO = "hello";
	private final static String C_REQUEST_JOIN_GAME = "request-join-game";
	private final static String C_UPDATE = "update";
	private final static String C_REQUEST_WORLD = "request-world";
	private final static String C_REQUEST_RESPAWN = "request-respawn";
	private final static String C_KEEP_ALIVE = "keep-alive";
	private final static String C_CHAT_MESSAGE = "chat-message";
	
	public GameServer(int port, int maxBots, int gameSize) {
		config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(port);
        ioServer = new SocketIOServer(config);
        gameController = new GameController(ioServer, maxBots, gameSize);
	}
	
	public void start() {
    	LOG.info("Gameserver started!");
        gameController.start();
        setupSocketListeners();                
        ioServer.start();
	}
	
	public void setupSocketListeners() {
		ioServer.addEventListener(C_HELLO, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	gameController.clientHelloEvent(client);
            }
        });
		
		ioServer.addEventListener(C_REQUEST_JOIN_GAME, ClientGameJoinDto.class, new ClientEventListener<ClientGameJoinDto>() {
            @Override
            public void onData(final SocketIOClient client, ClientGameJoinDto gameJoinDto, final AckRequest ackRequest) {
            	super.onData(client, gameJoinDto, ackRequest);
            	gameController.clientRequestGameJoinEvent(client, gameJoinDto);
            }
        });

		ioServer.addEventListener(C_KEEP_ALIVE, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	gameController.clientKeepAliveEvent(client);
            }
        });

        ioServer.addEventListener(C_REQUEST_WORLD, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	gameController.clientRequestWorldEvent(client);
            }
        });

        ioServer.addEventListener(C_REQUEST_RESPAWN, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	try {
					gameController.clientRequestRespawnEvent(client);
				} catch (Exception e) {
					LOG.warn("Client was null, this wasn't meant to happen");
					e.printStackTrace();
				}
            }
        });

        ioServer.addEventListener(C_UPDATE, ClientUpdateDto.class, new ClientEventListener<ClientUpdateDto>() {
            @Override
            public void onData(final SocketIOClient client, ClientUpdateDto updateDto, final AckRequest ackRequest) {
            	super.onData(client, updateDto, ackRequest);
            	if (updateDto != null)
					try {
						gameController.clientUpdateEvent(client, updateDto);
					} catch (Exception e) {
						LOG.warn("Client was null, this wasn't meant to happen");
						e.printStackTrace();
					}
            }
        });

        ioServer.addEventListener(C_CHAT_MESSAGE, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String message, final AckRequest ackRequest) {
            	super.onData(client, message, ackRequest);
            	try {
					gameController.clientChatMessageEvent(client, message);
				} catch (Exception e) {
					LOG.warn("Client was null, this wasn't meant to happen");
					e.printStackTrace();
				}
            }
        });
        
        ioServer.addDisconnectListener(new DisconnectListener() {
        	@Override
        	public void onDisconnect(final SocketIOClient client) {
            	gameController.clientDisconnectEvent(client);
        	}
        });
	}
	
	private class ClientEventListener<T> implements DataListener<T> {
		public void onData(SocketIOClient client, T data, AckRequest ackRequest) {
			//addLatency();
		}
		
		@SuppressWarnings("unused")
		void addLatency() {
        	try { Thread.sleep(LATENCY_THROTTLE); } catch (InterruptedException e) {}
		}
	}
	
}
