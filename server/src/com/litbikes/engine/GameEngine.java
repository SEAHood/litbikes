package com.litbikes.engine;

import java.awt.geom.Line2D;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
import com.litbikes.model.Player;
import com.litbikes.model.Spawn;
import com.litbikes.model.TrailSegment;
import com.litbikes.model.Wall;
import com.litbikes.util.Vector;

public class GameEngine {
	public static final double BASE_BIKE_SPEED = 1.5;
	
	private static Logger LOG = Log.getLogger(GameEngine.class);
	private static final int GAME_TICK_MS = 25;
	
	private GameEventListener eventListener;
	private long gameTick = 0;
		
	private final List<Player> players;
	private final Arena arena;
	private final ScoreKeeper score;
	private final int gameSize;	
	
	private Timer roundTimer;
	private int roundTimeLeft;
	private int timeUntilNextRound;
	private boolean roundInProgress = false;
	
	public GameEngine(int gameSize) {
		arena = new Arena(gameSize);
		players = new ArrayList<>();
		score = new ScoreKeeper();
		this.gameSize = gameSize;
	}
	
	public void start() {
		Runnable gameLoop = new GameTick();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		LOG.info("Starting game at " + GAME_TICK_MS + "ms per game tick");
		executor.scheduleAtFixedRate(gameLoop, 0, GAME_TICK_MS, TimeUnit.MILLISECONDS);
		eventListener.gameStarted();
	}
	
	// duration and delay = seconds
	public void startRound(int duration, int delay, boolean force) {
		if (!roundInProgress || force) {			
			
			timeUntilNextRound = delay;
			
			Timer countdownTimer = new Timer();
			TimerTask countdownTask = new TimerTask() {
				public void run() {
					timeUntilNextRound--;
				}
			};
			
			Timer delayTimer = new Timer();
			TimerTask delayTask = new TimerTask() {
				public void run() {
					
					roundTimeLeft = duration;
					roundTimer = new Timer();
					TimerTask t = new TimerTask() {
						public void run() {
							if (roundTimeLeft == 0) {
								endRound();
								return;
							}
							roundTimeLeft--;
						}
					};
					roundTimer.scheduleAtFixedRate(t, 1000, 1000);
					for (Player p : players) {
						p.getBike().init(findSpawn(), false);
					}
					score.reset();
					roundInProgress = true;
					countdownTimer.cancel();
					eventListener.roundStarted();
					LOG.info("Round started. Duration: " + duration + " seconds");
				}
			};
			
			if (delay > 0)
				countdownTimer.scheduleAtFixedRate(countdownTask, 1000, 1000);
			delayTimer.schedule(delayTask, delay * 1000);
		}
	}
	
	private void endRound() {
		roundTimer.cancel();
		roundInProgress = false;
		for (Player p : players) {
			p.getBike().setSpectating(true);
		}
		eventListener.roundEnded();
		LOG.info("Round ended - winner: " + score.getCurrentWinnerName());
	}
		
	public Player playerJoin(int pid, String name) {
		LOG.info("Creating new player with pid " + pid);
		
		Player player = new Player(pid);
		player.setName(name);
		
		Bike bike = new Bike(pid, name);
		bike.init(findSpawn(), true);
		player.setBike(bike);
		
		players.add(player);
		score.grantScore(pid, name, 0);
		return player;
	}
	
	public void dropPlayer(int pid) {
		Player player = players.stream()
				.filter(b -> b.getPid() == pid)
				.findFirst()
				.orElse(null);
		if (player == null)
			return;
		players.remove(player);
		score.removeScore(pid);
		LOG.info("Dropped player " + pid);
	}
	
	public boolean handleClientUpdate(ClientUpdateDto data) {
		if ( data.isValid() ) {			
			if ( players.size() > 0 ) {				
				Player player = players.stream()
						.filter(b -> b.getPid() == data.pid)
						.findFirst()
						.orElse(null);
				if (player == null)
					return false;
				
				Bike bike = player.getBike();
				bike.setDir( new Vector(data.xDir, data.yDir) );
				player.updateBike(bike);
			}						
			return true;
		} else 
			return false;
	}
	
	public ServerWorldDto getWorldDto() {
		ServerWorldDto worldDto = new ServerWorldDto();
		List<BikeDto> bikesDto = new ArrayList<>();

		for ( Player player : players ) {
			BikeDto bikeDto = player.getBike().getDto();
			bikeDto.score = score.getScore(bikeDto.pid);
			bikesDto.add( bikeDto );
		}

		Instant now = Instant.now();
		worldDto.timestamp = now.getEpochSecond();
		worldDto.roundInProgress = roundInProgress;
		worldDto.roundTimeLeft = roundTimeLeft;
		worldDto.timeUntilNextRound = timeUntilNextRound;
		worldDto.currentWinner = score.getCurrentWinner();
		worldDto.gameTick = gameTick;
		worldDto.arena = arena.getDto();
		worldDto.bikes = bikesDto;
		
		return worldDto;
	}
	
	public void attachListener( GameEventListener listener ) {
		eventListener = listener;
	}

	public void requestRespawn(int pid) {
		Player player = players.stream()
				.filter(b -> b.getPid() == pid)
				.findFirst()
				.orElse(null);
		if ( player != null && player.getBike().isCrashed() ) {
			player.respawn(findSpawn());
			eventListener.playerSpawned(pid);
		}		
	}
	
	public Spawn findSpawn() {
		Spawn spawn = new Spawn(gameSize);
		int i = 0;
		while (!spawnIsAcceptable(spawn) && i++ < 10) {
			spawn = new Spawn(gameSize);
		}
		return spawn;
	}
	
	public boolean spawnIsAcceptable(Spawn spawn) {

		int limit = 80; // Distance to nearest trail
		List<TrailSegment> trails = players.stream()
				.map(m -> m.getBike().getTrail(true))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		
		double aheadX = spawn.getPos().x + (limit * spawn.getDir().x);
		double aheadY = spawn.getPos().y + (limit * spawn.getDir().y);
		
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
	
		    	if (roundInProgress) {
		    		
			    	List<Player> activePlayers = players.stream()
			    			.filter(b -> b.isAlive())
			    			.collect(Collectors.toList());	
			    	List<Thread> threads = new ArrayList<>();
			    	for ( Player player : activePlayers ) {
			    		Thread t = new Thread(() -> {
			    			
			    			// Faster father from center - disabled temporarily
			    			/* 
			    			Point2D center = new Point2D.Double(gameSize/2, gameSize/2);
			    			Point2D bikePos = new Point2D.Double(bike.getPos().x, bike.getPos().y);
			    			double distance = bikePos.distance(center);
			    			double oldMin = 0;
			    			double oldMax = gameSize/2;
			    			double newMin = 0;
			    			double newMax = 0.5;
			    			double oldRange = oldMax - oldMin;
			    			double newRange = newMax - newMin;
	    					double spdModifier = ((distance - oldMin) * newRange / oldRange) + newMin; // Trust me
	    							
	    					bike.setSpd(BASE_BIKE_SPEED + spdModifier);*/
	    					Bike bike = player.getBike();
				    		bike.updatePosition();
							boolean collided = false;
			
							for ( Player p : activePlayers ) {
								boolean isSelf = p.getPid() == player.getPid();
								collided = collided || bike.collides( p.getBike().getTrail(!isSelf), 1 );
								if ( collided ) {
									bike.setCrashedInto(p.getBike());
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
									if ( !(crashedInto instanceof Wall) && crashedInto.getId() != bike.getPid() ) {
										score.grantScore(crashedInto.getId(), crashedInto.getName(), 1);
										eventListener.scoreUpdated(score.getScores());
									}
								}
							}	
							player.updateBike(bike);
			    		});
			    		
			    		threads.add(t);
			    		t.start();
			    	}
			    	
			    	for ( int i = 0; i < threads.size(); i++ )
			    		threads.get(i).join();
		    		
		    	}
		    	
			} catch (Exception e) {
				e.printStackTrace();
				LOG.info(e.getMessage());
			}
	    }
	}
	
	public Arena getArena() {
		return arena;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Player getPlayer(int pid) {
		return players.stream().filter(p -> p.getPid() == pid).findFirst().orElse(null);
	}

	public int getRoundTimeLeft() {
		return roundTimeLeft;
	}
	
}