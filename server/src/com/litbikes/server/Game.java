package com.litbikes.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.BikeDto;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.ServerWorldDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.util.Vector;

public class Game {
	private GameEventListener eventListener;
	private int pidGen = 0;
	private static final int GAME_TICK_MS = 30;
	private static final int GAME_WIDTH = 700;
	private static final int GAME_HEIGHT = 700;
	
	private List<Bike> bikes;
	private Arena arena;
	Logger log = Log.getLog();
	
	public Game() {
		Vector arenaDim = new Vector(GAME_WIDTH, GAME_HEIGHT);
		arena = new Arena(arenaDim);
		bikes = new ArrayList<>();
	}
	
	public void start() {
		Runnable gameLoop = new GameLoop();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		System.out.println("Starting game at " + GAME_TICK_MS + "ms per game tick");
		executor.scheduleAtFixedRate(gameLoop, 0, GAME_TICK_MS, TimeUnit.MILLISECONDS);
	}
	
	class GameLoop implements Runnable {
	    public void run() {
	    	for ( Bike bike : bikes ) {
	    		if ( bike.isCrashed() || bike.isSpectating() )
	    			continue;
	    		
	    		bike.updatePosition();
	    						
				boolean collided = false;
				for ( Bike b : bikes ) {
					List<Vector> trail = new ArrayList<>();
					trail.addAll( b.getTrail() );
					
					if ( bike.getPid() != b.getPid() ) 
						trail.add( b.getPos() );
					
					collided = bike.checkCollision( trail, b.getPid() == bike.getPid() ) || arena.checkCollision( bike );
				}
				
				if ( collided ) {
					bike.crash();
					bike.setSpectating(true);
					eventListener.playerCrashed(bike.getPid());
					
				}
				
	    	}
	    }
	}
	
	
	// Returns new pid
	public BikeDto newPlayer() {		
		int pid = this.pidGen++;
		log.info("Creating new player with pid " + pid);
		Bike newBike = Bike.create(pid);
		bikes.add( newBike );
		return newBike.getDto();
	}
	
	public void dropPlayer(int pid) {
		Bike bike = bikes.stream().filter(b -> b.getPid() == pid).findFirst().get();
		bikes.remove(bike);
		log.info("Dropped player " + pid);
	}
	
	public boolean handleClientUpdate(ClientUpdateDto data) {
		if ( data.isValid() ) {
			
			if ( bikes.size() > 0 ) {				
				Bike bike = bikes.stream().filter(b -> b.getPid() == data.pid).findFirst().get();
				
				if ( bike.setSpd( new Vector(data.xSpd, data.ySpd) ) )
					bike.addTrailPoint();				
			}
						
			return true;
		} else 
			return false;
	}
	
	public ServerWorldDto getWorldDto() {
		ServerWorldDto worldDto = new ServerWorldDto();
		List<BikeDto> bikesDto = new ArrayList<>();
		
		for ( Bike bike : bikes ) {
			bikesDto.add( bike.getDto() );
		}

		Instant now = Instant.now();
		worldDto.timestamp = now.getEpochSecond();
		worldDto.arena = arena.getDto();
		worldDto.bikes = bikesDto;
		return worldDto;
	}
	
	public void attachListener( GameEventListener listener ) {
		eventListener = listener;
	}

	public void requestRespawn(int pid) {

		Bike bike = bikes.stream().filter(b -> b.getPid() == pid).findFirst().get();
		if ( bike != null && bike.isCrashed() ) {
			bikes.remove(bike);
			bikes.add(Bike.create(pid));
			eventListener.playerSpawned(pid);
		}
		
	}

	int getGameTickMs() {
		return GAME_TICK_MS;
	}
	
}