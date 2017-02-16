package com.litbikes.server;

public class Launcher {
		
	public static void main(String[] args) {

		Thread gameServerThread = new Thread( new Runnable() {
			@Override
			public void run() {
				GameServer game = new GameServer(9092);
				game.start();	
			}
		}, "GameServer");
		
		Thread webServerThread = new Thread( new Runnable() {
			@Override
			public void run() {
				WebServer web = new WebServer(8080);
				web.start();
			}
		}, "WebServer");

		gameServerThread.start();
		webServerThread.start();
    	
	}
}
