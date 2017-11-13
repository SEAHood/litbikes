package com.litbikes.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.model.IPlayer;
import com.litbikes.model.TrailSegment;

public class Bot implements IPlayer {
	private static Logger LOG = Log.getLogger(Bot.class);
	private static int AI_TICK_MS = 50;
	private static int AI_RESPAWN_MS = 3000;
	private final int pid;
	private final BotIOClient ioClient;
	private Bike bike;
	private List<Bike> bikes;
	private Arena arena;
	long lastPredictionTime;
	long predictionCooldown = 200;
	BotController controller;
	
	public Bot( Bike _bike, List<Bike> _bikes, Arena _arena ) {
		pid = _bike.getPid();
		bike = _bike;
		arena = _arena;
		bikes = _bikes;
		ioClient = new BotIOClient(this);
	}
	
	public int getPid() {
		return pid;
	}
	
	public String getName() {
		return bike.getName(); //todo lazy lol
	}
	
	public Bike getBike() {
		return bike;
	}
	
	public void attachController( BotController _controller ) {
		controller = _controller;
	}
	
	public void updateWorld( List<Bike> _bikes, Arena _arena ) {
		arena = _arena;
		bikes = _bikes;
	}
	
	public void start() {
		Runnable aiLoop = new AILoop();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		LOG.info("Starting bot at " + AI_TICK_MS + "ms per AI tick");
		executor.scheduleAtFixedRate(aiLoop, 0, AI_TICK_MS, TimeUnit.MILLISECONDS);
	}
	
	private void predictCollision() {
		long thisPredictionTime = System.currentTimeMillis();
		if ( thisPredictionTime - lastPredictionTime > predictionCooldown ) {
	 		int dDist = 20;
 			List<Bike> activeBikes = bikes.stream().filter(b -> b.isActive()).collect(Collectors.toList());
    	  		
			List<TrailSegment> allTrails = new ArrayList<>();
			for ( Bike b : activeBikes ) {
				boolean isSelf = b.getPid() == pid;
				allTrails.addAll( b.getTrail(!isSelf) );
			}
			
	 		boolean incCollision = bike.checkCollision( allTrails, dDist ) || arena.checkCollision(bike, dDist);
			
			if (incCollision) {
	 			int newVal = Math.random() < 0.5 ? -1 : 1;
	 			
	 			ClientUpdateDto updateDto = new ClientUpdateDto();
	 			updateDto.pid = pid;
	 			
	 			if ( bike.getSpd().x != 0 ) {
	 				updateDto.xSpd = 0;
	 				updateDto.ySpd = newVal;
	 			} else if ( bike.getSpd().y != 0 ) {
	 				updateDto.xSpd = newVal;
	 				updateDto.ySpd = 0;
	 			}
	 			
	 			controller.sentUpdate(ioClient, updateDto);
			}		
	 					
			lastPredictionTime = thisPredictionTime;
		}	
	}

	public UUID getSessionId() {
		return ioClient.getSessionId();
	}

	class AILoop implements Runnable {
	    public void run() {
	    	try {
		    	if ( !bike.isCrashed() ) {
			    	predictCollision();
		    	} else {
		    		try {
						Thread.sleep(AI_RESPAWN_MS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    		controller.sentRequestRespawn(ioClient);
		    	}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.info(e.getMessage());
			} 	
	    }
	}
	
}
