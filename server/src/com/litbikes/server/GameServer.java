package com.litbikes.server;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.engine.GameController;

public class GameServer {
	
	private static Logger LOG = Log.getLogger(GameServer.class);
	private SocketIOServer ioServer;	
	private Configuration config;
	private GameController gameController;
	
	private final int LATENCY_THROTTLE = 60;

	private final static String C_REGISTER = "register";
	private final static String C_UPDATE = "update";
	private final static String C_REQUEST_WORLD = "request-world";
	private final static String C_REQUEST_RESPAWN = "request-respawn";
	private final static String C_KEEP_ALIVE = "keep-alive";
	private final static String C_CHAT_MESSAGE = "chat-message";
	
	public GameServer(int port) {
		config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(port);
        ioServer = new SocketIOServer(config);
        gameController = new GameController(ioServer);
	}
	
	public void start() {
    	LOG.info("Gameserver started!");
        gameController.start();
        setupSocketListeners();                
        ioServer.start();
	}
	
	public void setupSocketListeners() {
		ioServer.addEventListener(C_REGISTER, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	super.onData(client, data, ackRequest);
            	gameController.clientRegisterEvent(client);
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
            	gameController.clientRequestRespawnEvent(client);
            }
        });

        ioServer.addEventListener(C_UPDATE, ClientUpdateDto.class, new ClientEventListener<ClientUpdateDto>() {
            @Override
            public void onData(final SocketIOClient client, ClientUpdateDto updateDto, final AckRequest ackRequest) {
            	super.onData(client, updateDto, ackRequest);
            	gameController.clientUpdateEvent(client, updateDto);
            }
        });

        ioServer.addEventListener(C_CHAT_MESSAGE, String.class, new ClientEventListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String message, final AckRequest ackRequest) {
            	super.onData(client, message, ackRequest);
            	gameController.clientChatMessageEvent(client, message);
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
