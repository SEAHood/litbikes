package com.litbikes.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.model.TrailSegment;

public class Bot {
	private static Logger LOG = Log.getLogger(Bot.class);
	private static int AI_TICK_MS = 50;
	private static int AI_RESPAWN_MS = 3000;
	private BotListener listener;
	private final int pid;
	private Bike botBike;
	private List<Bike> bikes;
	private Arena arena;
	long lastPredictionTime;
	long predictionCooldown = 200;
	
	public Bot( Bike bike, List<Bike> bikes, Arena arena ) {
		this.pid = bike.getPid();
		this.botBike = bike;
		this.arena = arena;
		this.bikes = bikes;
	}
	
	public void updateWorld( List<Bike> bikes, Arena arena ) {
		this.arena = arena;
		this.bikes = bikes;
	}
	
	public void start() {
		Runnable aiLoop = new AILoop(this);
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
			
	 		boolean incCollision = botBike.checkCollision( allTrails, dDist ) || arena.checkCollision(botBike, dDist);
			
			if (incCollision) {
	 			int newVal = Math.random() < 0.5 ? -1 : 1;
	 			
	 			ClientUpdateDto updateDto = new ClientUpdateDto();
	 			updateDto.pid = pid;
	 			
	 			if ( botBike.getSpd().x != 0 ) {
	 				updateDto.xSpd = 0;
	 				updateDto.ySpd = newVal;
	 			} else if ( botBike.getSpd().y != 0 ) {
	 				updateDto.xSpd = newVal;
	 				updateDto.ySpd = 0;
	 			}
	 			
	 			listener.sentClientUpdate(updateDto);
			}		
	 					
			lastPredictionTime = thisPredictionTime;
		}	
     }
	
	public void attachListener( BotListener listener ) {
		this.listener = listener;
	}


	public int getPid() {
		return pid;
	}
		

	class AILoop implements Runnable {
		private Bot bot;
		public AILoop(Bot b) { bot = b; }
	    public void run() {
	    	listener.requestedWorldUpdate(bot);
	    	if ( !botBike.isCrashed() ) {
		    	predictCollision();
	    	} else {
	    		try {
					Thread.sleep(AI_RESPAWN_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    		listener.sentRequestRespawn(bot);
	    	}	    	
	    }
	}

	
}
