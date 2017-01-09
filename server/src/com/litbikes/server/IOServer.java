package com.litbikes.server;

import java.util.ArrayList;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.ServerWorldDto;
import com.litbikes.dto.Test;

public class IOServer {
	
	private final static String REGISTER = "register";
	private final static String CLIENT_UPDATE = "client-update";
	
	private static Game game;
	
	public static void main(String[] args) throws InterruptedException {

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        

        final SocketIOServer ioServer = new SocketIOServer(config);
        
        ioServer.addEventListener(REGISTER, String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	System.out.println("Received register event");
            	ioServer.getBroadcastOperations().sendEvent("test-event", "success");
            }
        });

        ioServer.addEventListener(CLIENT_UPDATE, ClientUpdateDto.class, new DataListener<ClientUpdateDto>() {
            @Override
            public void onData(final SocketIOClient client, ClientUpdateDto data, final AckRequest ackRequest) {
            	System.out.println("Received update event");
            	System.out.println(data.pid);
            	System.out.println(data.getPos().x);
            	System.out.println(data.getPos().y);
            	System.out.println(data.getSpd().x);
            	System.out.println(data.getSpd().y);
            	System.out.println(data.dead);
            	if ( game.handleClientUpdate(data) ) {
            		ServerWorldDto worldDto = new ServerWorldDto();
            		worldDto.number = 1;
            		worldDto.bool = true;
            		worldDto.text = "serverWorldDto";
            		worldDto.test = new Test("test obj a", "test obj b");
            		worldDto.testList = new ArrayList<Test>();
            		Test a = new Test("test list 1st obj a", "test list 1st obj b");
            		Test b = new Test("test list 1st obj a", "test list 1st obj b");
            		worldDto.testList.add(a);
            		worldDto.testList.add(b);
            		
            		ioServer.getBroadcastOperations().sendEvent("world-update", worldDto);
            		
            		//WorldUpdateDto worldUpdate 
            	}
            	//ioServer.getBroadcastOperations().sendEvent("test-event", "success");
                /*this.gameWorld.handleUpdate( data );
                this.worldUpdated();*/
            }
        });
        
        /*ioServer.addEventListener(CLIENT_UPDATE, String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
            	System.out.println(data)
            	//ioServer.getBroadcastOperations().sendEvent("test-event", "success");
                this.gameWorld.handleUpdate( data );
                this.worldUpdated();
            }
        });*/
        
        ioServer.addDisconnectListener(new DisconnectListener() {
        	@Override
        	public void onDisconnect(final SocketIOClient client) {
        		//client.getSessionId();
        	}
        });

        
        // BROADCAST TO ALL
        // server.getBroadcastOperations().sendEvent("chatevent", data);

    	System.out.println("Server starting");
        ioServer.start();

    	game = Game.create();
    	game.start();
    	
        //Thread.sleep(Integer.MAX_VALUE);

        //server.stop();
	}
        
}
