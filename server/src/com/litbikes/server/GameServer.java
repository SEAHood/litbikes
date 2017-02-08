package com.litbikes.server;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

public class GameServer {
	
	private static Logger LOG = Log.getLogger(GameServer.class);
	private static Game game;
	private static SocketIOServer ioServer;	
	
	public static void main(String[] args) throws InterruptedException {

        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(9092);
        
        ioServer = new SocketIOServer(config);
        game = new Game();
        GameController gameController = new GameController(ioServer, game);
        gameController.initialise();
                
        ioServer.start();
    	game.start();
    	

    	LOG.info("Gameserver started!");
	}
	
	
}
