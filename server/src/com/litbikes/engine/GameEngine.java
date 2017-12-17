package com.litbikes.engine;

import java.awt.geom.Line2D;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.dto.PlayerDto;
import com.litbikes.dto.PowerUpDto;
import com.litbikes.dto.ScoreDto;
import com.litbikes.dto.ServerWorldDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.model.ICollidable;
import com.litbikes.model.Player;
import com.litbikes.model.PowerUp;
import com.litbikes.model.PowerUp.PowerUpType;
import com.litbikes.model.Spawn;
import com.litbikes.model.TrailSegment;
import com.litbikes.model.Wall;
import com.litbikes.model.Player.PlayerEffect;
import com.litbikes.util.Vector;

public class GameEngine {
	public static final double BASE_BIKE_SPEED = 1.5;
	
	private static Logger LOG = Log.getLogger(GameEngine.class);
	private static final int GAME_TICK_MS = 25;
	private static final int PU_SPAWN_DELAY_MIN = 10;
	private static final int PU_SPAWN_DELAY_MAX = 20;
	private static final int PU_DURATION_MIN = 10;
	private static final int PU_DURATION_MAX = 20;
	
	private GameEventListener eventListener;
	private long gameTick = 0;
		
	private final List<Player> players;
	private final CopyOnWriteArrayList<PowerUp> powerUps;
	private final Arena arena;
	private final ScoreKeeper score;
	private final int gameSize;	
	
	private Timer roundTimer;
	private int roundTimeLeft;
	private int timeUntilNextRound;
	private boolean roundInProgress = false;
	
	private Timer powerUpSpawnTimer = new Timer();
	
	public GameEngine(int gameSize) {
		arena = new Arena(gameSize);
		players = new ArrayList<>();
		powerUps = new CopyOnWriteArrayList<>();
		score = new ScoreKeeper();
		this.gameSize = gameSize;
	}
	
	public void start() {
		Runnable gameLoop = new GameTick();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(gameLoop, 0, GAME_TICK_MS, TimeUnit.MILLISECONDS);
		
		PowerUpSpawner powerUpSpawner = new PowerUpSpawner();
		powerUpSpawner.run();
		
		LOG.info("Starting game at " + GAME_TICK_MS + "ms per game tick");
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
					try {
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
							p.respawn(findSpawn());
						}
						score.reset();
						roundInProgress = true;
						countdownTimer.cancel();
						eventListener.roundStarted();
						LOG.info("Round started. Duration: " + duration + " seconds");
					} catch (Exception e) {
						e.printStackTrace();
					}
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
			p.setSpectating(true);
		}
		eventListener.roundEnded();
		LOG.info("Round ended - winner: " + score.getCurrentWinnerName());
	}
		
	public Player playerJoin(int pid, String name, boolean isHuman) {
		LOG.info("Creating new player with pid " + pid);
		
		Player player = new Player(pid, isHuman);
		player.setName(name);
		
		Bike bike = new Bike();
		bike.init(findSpawn(), true);
		player.setBike(bike);
		
		players.add(player);
		score.grantScore(pid, name, 0);
		return player;
	}
	
	public void dropPlayer(int pid) {
		Player player = players.stream()
				.filter(p -> p.getId() == pid)
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
						.filter(p -> p.getId() == data.pid)
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
		List<PlayerDto> playersDto = new ArrayList<>();
		for ( Player player : players ) {
			PlayerDto playerDto = player.getDto();
			playerDto.score = score.getScore(playerDto.pid);
			playersDto.add( playerDto );
		}

		List<PowerUpDto> powerUpsDto = new ArrayList<>();
		for ( PowerUp p : powerUps ) {
			PowerUpDto powerUpDto = p.getDto();
			powerUpsDto.add(powerUpDto);
		}

		Instant now = Instant.now();
		worldDto.timestamp = now.getEpochSecond();
		worldDto.roundInProgress = roundInProgress;
		worldDto.roundTimeLeft = roundTimeLeft;
		worldDto.timeUntilNextRound = timeUntilNextRound;
		worldDto.currentWinner = score.getCurrentWinner();
		worldDto.gameTick = gameTick;
		worldDto.arena = arena.getDto();
		worldDto.players = playersDto;
		worldDto.powerUps = powerUpsDto;
		
		return worldDto;
	}
	
	public void attachListener( GameEventListener listener ) {
		eventListener = listener;
	}

	public void requestRespawn(Player player) {
		if ( player != null && player.isCrashed() ) {
			player.respawn(findSpawn());
			eventListener.playerSpawned(player.getId());
		}		
	}

	public void requestUsePowerUp(Player player) {
		if ( player != null && player.isAlive() && player.getCurrentPowerUpType() != null ) {
			switch (player.getCurrentPowerUpType()) {
				case ROCKET:
					break;
				case SLOW:
					players.stream().forEach(p -> {
						if (p.getId() != player.getId()) {
							Bike b = p.getBike();
							double oldSpd = b.getSpd();
							b.setSpd(0.5);
							p.updateBike(b);
							p.setEffect(PlayerEffect.SLOWED);

							Timer timer = new Timer();
							TimerTask task = new TimerTask() {
								public void run() {
									b.setSpd(oldSpd);
									p.updateBike(b);
									p.setEffect(PlayerEffect.NONE);
								}
							};
							timer.schedule(task, 3000);
						}
					});
					
					break;
				default:
					return;				
			}
			player.setCurrentPowerUpType(null);
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

	public int getGameTickMs() {
		return GAME_TICK_MS;
	}
	
	public Arena getArena() {
		return arena;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Player getPlayer(int pid) {
		return players.stream().filter(p -> p.getId() == pid).findFirst().orElse(null);
	}

	public int getRoundTimeLeft() {
		return roundTimeLeft;
	}
	
	class PowerUpSpawner extends TimerTask {
		@Override
		public void run() {
			try {
				PowerUpType type = PowerUpType.SLOW;
				/*int rand = new Random().nextInt(2);
				if (rand == 1)
					type = PowerUpType.ROCKET;
				else
					type = PowerUpType.SLOW;*/
				
				PowerUp powerUp = new PowerUp(Vector.random(gameSize, gameSize), type);
				powerUps.add(powerUp);				
	            int delay = (PU_SPAWN_DELAY_MIN + new Random().nextInt(PU_SPAWN_DELAY_MAX)) * 1000;
	            powerUpSpawnTimer.schedule(new PowerUpSpawner(), delay);
	            
	            // Schedule despawn of powerup
	            int duration = (PU_DURATION_MIN + new Random().nextInt(PU_DURATION_MAX)) * 1000;
	            powerUpSpawnTimer.schedule(new TimerTask() {
	            	@Override
	            	public void run() {
	            		if (!powerUp.isCollected()) {
    	            		powerUps.remove(powerUp);
	            		}
	            	}
	            }, duration);
			} catch (Exception ex) {
				LOG.warn("Exception thrown in power up manager", ex);
			}
		}
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
			    			
				    		player.update();
				    		
				    		Bike playerBike = player.getBike();
							boolean collided = false;
							ICollidable collidedWith = null;
							PowerUp powerUpCollected = null;
							
			
							for ( Player p : activePlayers ) {
								boolean isSelf = p.getId() == player.getId();
								collided = collided || playerBike.collides( p.getBike().getTrail(!isSelf), 1 );
								if ( collided ) {
									collidedWith = p;
									break;
								}
								
								for (PowerUp powerUp : powerUps) {
									Vector pos = p.getBike().getPos();
									double aheadX = pos.x + (2 * p.getBike().getDir().x);
									double aheadY = pos.y + (2 * p.getBike().getDir().y);
									Line2D line = new Line2D.Double(pos.x, pos.y, aheadX, aheadY);
									if (powerUp.collides(line))  {
										powerUpCollected = powerUp;
										p.setCurrentPowerUpType(powerUp.getType());
										break;
									}
								}
							}
							
							if ( !collided && arena.checkCollision(playerBike, 1) ) {
								collided = true;
								collidedWith = new Wall();				
							}
			
							if ( collided ) {
								player.crashed(collidedWith);
								eventListener.playerCrashed(player);
								if ( !(collidedWith instanceof Wall) && collidedWith.getId() != player.getId() ) {
									score.grantScore(collidedWith.getId(), collidedWith.getName(), 1);
									eventListener.scoreUpdated(score.getScores());
								}
							}	
													
							if (powerUpCollected != null) {
								powerUpCollected.setCollected(true);
								final PowerUp powerUp = powerUpCollected;	
								Timer timer = new Timer();
								timer.schedule(new TimerTask() {
			    	            	@Override
			    	            	public void run() {
			    	            		try {
				    	            		powerUps.remove(powerUp);			    	            			
			    	            		} catch (Exception e) {
			    	            			e.printStackTrace();
			    	            		}
			    	            	}
			    	            }, 4000);
							}
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
}