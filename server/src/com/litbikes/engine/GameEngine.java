package com.litbikes.engine;

import java.awt.geom.Line2D;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.BikeDto;
import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.ScoreDto;
import com.litbikes.dto.ServerWorldDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.model.ICollidable;
import com.litbikes.model.Spawn;
import com.litbikes.model.TrailSegment;
import com.litbikes.model.Wall;
import com.litbikes.util.Vector;

public class GameEngine {
	private static Logger LOG = Log.getLogger(GameEngine.class);
	private static final int GAME_TICK_MS = 25;
	
	private GameEventListener eventListener;
	private long gameTick = 0;
		
	private final List<Bike> bikes;
	private final Arena arena;
	private final ScoreKeeper score;

	private final int gameWidth;
	private final int gameHeight;
	
	public GameEngine(int gameWidth, int gameHeight) {
		Vector arenaDim = new Vector(gameWidth, gameHeight);
		arena = new Arena(arenaDim);
		bikes = new ArrayList<>();
		score = new ScoreKeeper();
		this.gameWidth = gameWidth;
		this.gameHeight = gameHeight;
	}
	
	public void start() {
		Runnable gameLoop = new GameTick();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		LOG.info("Starting game at " + GAME_TICK_MS + "ms per game tick");
		executor.scheduleAtFixedRate(gameLoop, 0, GAME_TICK_MS, TimeUnit.MILLISECONDS);
		eventListener.gameStarted();
	}
	
	public Bike playerJoin(int pid, String name) {
		LOG.info("Creating new player with pid " + pid);
		Bike bike = new Bike(pid, name);
		bike.init(findSpawn(), true);
		bikes.add(bike);
		score.grantScore(pid, name, 0);
		return bike;
	}
	
	public void dropPlayer(int pid) {
		Bike bike = bikes.stream().filter(b -> b.getPid() == pid).findFirst().get();
		bikes.remove(bike);
		score.removeScore(pid);
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
			BikeDto bikeDto = bike.getDto();
			bikeDto.score = score.getScore(bikeDto.pid);
			bikesDto.add( bikeDto );
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
			bike.init(findSpawn(), false);
			eventListener.playerSpawned(pid);
		}		
	}
	
	public Spawn findSpawn() {
		Spawn spawn = new Spawn(gameWidth, gameHeight);
		int i = 0;
		while (!spawnIsAcceptable(spawn) && i++ < 10) {
			spawn = new Spawn(gameWidth, gameHeight);
		}
		return spawn;
	}
	
	public boolean spawnIsAcceptable(Spawn spawn) {

		int limit = 50; // Distance to nearest trail
		List<TrailSegment> trails = bikes.stream().map(m -> m.getTrail(true)).flatMap(Collection::stream).collect(Collectors.toList());
		
		double aheadX = spawn.getPos().x + (limit * spawn.getSpd().x);
		double aheadY = spawn.getPos().y + (limit * spawn.getSpd().y);
		
		Line2D line = new Line2D.Double(spawn.getPos().x, spawn.getPos().y, aheadX, aheadY);
					
		for ( TrailSegment segment : trails ) {
			if ( line.intersectsLine(segment.getLine()) ) 
				return false;	
		}
		
		return true;
	}
	
	public List<ScoreDto> getScores() {
		return score.getScores();
	}

	int getGameTickMs() {
		return GAME_TICK_MS;
	}
	
	class GameTick implements Runnable {
		
		long tim = System.currentTimeMillis();
		
		public void run() {
			try {
		    	//Increment tick count first
		    	gameTick++;
	
		    	List<Bike> activeBikes = bikes.stream().filter(b -> b.isActive()).collect(Collectors.toList());
	
		    	List<Thread> threads = new ArrayList<>();
		    	for ( Bike bike : activeBikes ) {
		    		Thread t = new Thread(() -> {
			    		bike.updatePosition(1);
						boolean collided = false;
		
						for ( Bike b : activeBikes ) {
							boolean isSelf = b.getPid() == bike.getPid();
							collided = collided || bike.collides( b.getTrail(!isSelf), 1 );
							if ( collided ) {
								bike.setCrashedInto(b);
								break;
							}
						}
						
						if ( arena.checkCollision(bike, 1) ) {
							collided = true;
							bike.setCrashedInto(new Wall());						
						}
		
						if ( collided ) {
							bike.crash();
							bike.setSpectating(true);
							eventListener.playerCrashed(bike);
							if ( bike.getCrashedInto() != null ) {
								ICollidable crashedInto = bike.getCrashedInto();
								if ( crashedInto.getId() != bike.getPid() ) {
									score.grantScore(crashedInto.getId(), crashedInto.getName(), 1);
									eventListener.scoreUpdated(score.getScores());
								}
							}
						}	
		    		});
		    		
		    		threads.add(t);
		    		t.start();
		    	}
		    	
		    	for ( int i = 0; i < threads.size(); i++ )
		    		threads.get(i).join();
		    	
			} catch (Exception e) {
				e.printStackTrace();
				LOG.info(e.getMessage());
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