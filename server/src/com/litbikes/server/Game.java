package com.litbikes.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.BikeDto;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.ServerWorldDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.util.Vector;

public class Game {
	private static Logger LOG = Log.getLogger(Game.class);
	private static final int GAME_TICK_MS = 30;
	private static final int GAME_WIDTH = 600;
	private static final int GAME_HEIGHT = 600;
	
	private GameEventListener eventListener;
	private int pidGen = 0;
	private long gameTick = 0;
		
	private List<Bike> bikes;
	private Arena arena;
	
	public Game() {
		Vector arenaDim = new Vector(GAME_WIDTH, GAME_HEIGHT);
		arena = new Arena(arenaDim);
		bikes = new ArrayList<>();
	}
	
	public void start() {
		Runnable gameLoop = new GameTick();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		LOG.info("Starting game at " + GAME_TICK_MS + "ms per game tick");
		executor.scheduleAtFixedRate(gameLoop, 0, GAME_TICK_MS, TimeUnit.MILLISECONDS);
		eventListener.gameStarted();
	}
	
	
	
	public Bike newPlayer() {		
		int pid = this.pidGen++;
		LOG.info("Creating new player with pid " + pid);
		Bike newBike = new Bike(pid);
		newBike.init();
		bikes.add( newBike );
		return newBike;
	}
	
	public void dropPlayer(int pid) {
		Bike bike = bikes.stream().filter(b -> b.getPid() == pid).findFirst().get();
		bikes.remove(bike);
		LOG.info("Dropped player " + pid);
	}
	
	public boolean handleClientUpdate(ClientUpdateDto data) {
		if ( data.isValid() ) {			
			if ( bikes.size() > 0 ) {				
				Bike bike = bikes.stream().filter(b -> b.getPid() == data.pid).findFirst().get();				
				bike.setSpd( new Vector(data.xSpd, data.ySpd) );
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
		worldDto.gameTick = gameTick;
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
			bike.init();
			eventListener.playerSpawned(pid);
		}		
	}

	int getGameTickMs() {
		return GAME_TICK_MS;
	}
	
	class GameTick implements Runnable {
		
		long tim = System.currentTimeMillis();
		
	    public void run() {
	    	//Increment tick count first
	    	gameTick++;

			long cTim = System.currentTimeMillis();
			if ( cTim - tim > 1000 )
			{
				//LOG.info(bikes.get(0).toString());
				tim = cTim;
			}
	    	
	    	List<Bike> activeBikes = bikes.stream().filter(b -> b.isActive()).collect(Collectors.toList());
	    	
	    	for ( Bike bike : activeBikes ) {
	    		bike.updatePosition();
				boolean collided = false;

				for ( Bike b : activeBikes ) {
					boolean isSelf = b.getPid() == bike.getPid();
					collided = collided || bike.checkCollision( b.getTrail(!isSelf), 1 );
					if ( collided ) {
						bike.setCrashedInto(Integer.toString(b.getPid()));
						break;
					}
				}
				
				if ( !collided ) {
					collided = arena.checkCollision(bike, 1);
					if ( collided )
						bike.setCrashedInto("wall");
				}

				if ( collided ) {
					bike.crash();
					bike.setSpectating(true);
					eventListener.playerCrashed(bike);
				}				
	    	}
	    }
	}

	public Arena getArena() {
		return arena;
	}

	public List<Bike> getBikes() {
		return bikes;
	}
	
}