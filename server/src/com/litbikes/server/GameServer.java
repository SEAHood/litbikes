package com.litbikes.server;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

public class GameServer {
	
	private static Game game;
	private static SocketIOServer ioServer;	
	
	public static void main(String[] args) throws InterruptedException {

        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(9092);
        
        ioServer = new SocketIOServer(config);
        game = new Game();
        EventLayer eventLayer = new EventLayer(ioServer, game);
        eventLayer.initialise();        
        
        ioServer.start();      
    	game.start();

    	System.out.println("Gameserver started!");
        //Thread.sleep(Integer.MAX_VALUE);

        //server.stop();
	}
	
	
}
