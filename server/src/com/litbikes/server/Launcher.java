package com.litbikes.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Launcher {
		
	public static void main(String[] args) {

		Map<String, List<String>> params = parseParams(args);
		if ( params == null )
			return;

		int port = 8080;
		int maxBots = 5;
		int gameWidth = 600;
		int gameHeight = 600;

		if ( params.get("p") != null ) // bots
			port = Integer.parseInt( params.get("p").get(0) );
		
		if ( params.get("b") != null ) // bots
			maxBots = Integer.parseInt( params.get("b").get(0) );
				
		if ( params.get("s") != null && params.get("s").size() == 2) { // game size
			gameWidth = Integer.parseInt( params.get("s").get(0) );
			gameHeight = Integer.parseInt( params.get("s").get(1) );			
		}
		
		final Integer _port = new Integer(port);
		final Integer _maxBots = new Integer(maxBots); 
		final Integer _gameWidth = new Integer(gameWidth); 
		final Integer _gameHeight = new Integer(gameHeight); 
		
		Thread gameServerThread = new Thread( new Runnable() {
			@Override
			public void run() {
				GameServer game = new GameServer(9092, _maxBots, _gameWidth, _gameHeight);
				game.start();	
			}
		}, "GameServer");
		
		Thread webServerThread = new Thread( new Runnable() {
			@Override
			public void run() {
				WebServer web = new WebServer(_port);
				web.start();
			}
		}, "WebServer");

		gameServerThread.start();
		webServerThread.start();
    	
	}
	
	private static Map<String, List<String>> parseParams(String[] args) {
		final Map<String, List<String>> params = new HashMap<>();

		List<String> options = null;
		for (int i = 0; i < args.length; i++) {
		    final String a = args[i];

		    if (a.charAt(0) == '-') {
		        if (a.length() > 2) {
		            System.err.println("Error at argument " + a);
		            return null;
		        }

		        options = new ArrayList<>();
		        params.put(a.substring(1), options);
		    }
		    else if (options != null) {
		        options.add(a);
		    }
		    else {
		        System.err.println("Illegal parameter usage");
		        return null;
		    }
		}
		
		return params;
	}
}
