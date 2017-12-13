package com.litbikes.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Player;
import com.litbikes.model.TrailSegment;

public class Bot extends Player {
	private static Logger LOG = Log.getLogger(Bot.class);
	private static int AI_TICK_MS = 50;
	private static int AI_RESPAWN_MS = 3000;
	private final BotIOClient ioClient;
	private CopyOnWriteArrayList<Player> players;
	private Arena arena;
	long lastPredictionTime;
	long predictionCooldown = 100;
	BotController controller;
	
	public Bot( int pid, List<Player> _players, Arena _arena ) {
		super(pid, false);
		arena = _arena;
		players = new CopyOnWriteArrayList<>(_players);
		ioClient = new BotIOClient(this);
	}
	
	public void attachController( BotController _controller ) {
		controller = _controller;
	}
	
	public void updateWorld(List<Player> _players, Arena _arena) {
		arena = _arena;
		players = new CopyOnWriteArrayList<>(_players);
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
 			List<Player> activePlayers = players.stream().filter(b -> b.isAlive()).collect(Collectors.toList());
    	  		
			List<TrailSegment> allTrails = new ArrayList<>();
			for ( Player p : activePlayers ) {
				boolean isSelf = p.getId() == pid;
				allTrails.addAll( p.getBike().getTrail(!isSelf) );
			}
			
	 		boolean incCollision = bike.collides( allTrails, dDist ) || arena.checkCollision(bike, dDist);
			
			if (incCollision) {
	 			int newVal = Math.random() < 0.5 ? -1 : 1;
	 			
	 			ClientUpdateDto updateDto = new ClientUpdateDto();
	 			updateDto.pid = pid;
	 			
	 			if ( bike.getDir().x != 0 ) {
	 				updateDto.xDir = 0;
	 				updateDto.yDir = newVal;
	 			} else if ( bike.getDir().y != 0 ) {
	 				updateDto.xDir = newVal;
	 				updateDto.yDir = 0;
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
	    		
		    	if ( !isCrashed() ) {
		    		try {
				    	predictCollision();
					} catch (Exception e) {
						e.printStackTrace();
					}
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
