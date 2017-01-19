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
import com.litbikes.dto.Test;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.util.Vector;

public class Game {
	private GameEventListener eventListener;
	private int pidGen = 0;
	private static final double FPS = 60.0;
	public static final double SPEED_MAGNITUDE = 0.4;
	
	private List<Bike> bikes;
	private Arena arena;
	Logger log = Log.getLog();
	
	public Game() {
		Vector arenaDim = new Vector(500, 500);
		arena = new Arena(arenaDim);
		bikes = new ArrayList<>();
	}
	
	public void start() {
		Runnable gameLoop = new GameLoop();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		long loopDurationMs = (long) (1/FPS * 1000);
		executor.scheduleAtFixedRate(gameLoop, 0, loopDurationMs, TimeUnit.MILLISECONDS);
	}
	
	class GameLoop implements Runnable {
	    public void run() {
	    	//TODO Implement actual game loop
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
					
					if ( collided ) {
						System.out.println("Collided!");
						break;
					}
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
			
			//don't care about anything else tbh
			
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
		worldDto.test = new Test("a","b");
		return worldDto;
	}
	
	public void attachListener( GameEventListener listener ) {
		eventListener = listener;
	}

	public void requestRespawn(int pid) {

		Bike bike = bikes.stream().filter(b -> b.getPid() == pid).findFirst().get();
		if ( bike != null ) {
			bikes.remove(bike);
			bikes.add(Bike.create(pid));
			eventListener.playerSpawned(pid);
		}
		
	}
	
	//TODO transport object
	/*public GameStateDto getGameStateDto() {
		
	}*/
	

//        constructor() {
//            let arenaDimensions = new Vector(500, 500);
//            this.arena = new Arena(arenaDimensions);
//            this.cycle();
//        }
//
//        cycle() {
//            if (this.isRunning) {
//                // dont console log crap here unless you want lag
//                //console.log(new Date());
//
//                // TODO GAME CYCLE
//            }
//
//            setTimeout(() => this.cycle(), 1 / this.fps)
//        }
//
//        public handleUpdate(dto: BikeDto) {
//            let bike = this.getBike(dto.pid);
//            if (bike) {
//                bike.spd = dto.spd;
//            } else {
//                this.addBike(new Bike(dto));
//            }
//        }
//
//
//        public newBike(): number {
//            let newBike = this.createBike();
//            this.addBike(newBike);
//            return newBike.getPid();
//        }
//
//        private createBike(): Bike {
//            let pid = this.pidGen++;
//            let pos = new Vector(Util.randInt(20, 480), Util.randInt(20, 480));
//            let spd = null;
//            var direction = Util.randInt(1, 4);
//            switch (direction) {
//                case 1:
//                    spd = new Vector(0, -1);
//                    break;
//                case 2:
//                    spd = new Vector(0, 1);
//                    break;
//                case 3:
//                    spd = new Vector(-1, 0);
//                    break;
//                case 4:
//                    spd = new Vector(1, 0);
//                    break;
//                default:
//                    break;
//            }
//
//            let bike = <BikeDto>{
//                pid: pid,
//                pos: pos,
//                spd: spd
//            };
//            return new Bike(bike);
//        }
//
//        public addBike(bike: Bike) {
//            this.bikes.push(bike);
//        }
//
//        //async
//        public removeBike(pid: number) {
//            //let tl = new ThreadedLock("bikes-lock");
//            //await tl.lock();
//            this.bikes = _.filter(this.bikes, (b: Bike) => b.pid !== pid);
//            //tl.unlock();
//        }
//
//        public getBike(pid: number): Bike {
//            return _.find(this.bikes, b => b.pid === pid);
//        }
//
//        private getBikeDtos(): BikeDto[] {
//            let bikeDtos: BikeDto[] = [];
//            _.each(this.bikes, bike => {
//                bikeDtos.push(
//                    <BikeDto>{
//                        pid: bike.pid,
//                        pos: bike.pos,
//                        spd: bike.spd,
//                        isDead: bike.isDead,
//                        deathTimestamp: bike.deathTimestamp,
//                        trail: bike.trail
//                    }
//                );
//            });
//            return bikeDtos;
//        }
//
//        private getArenaDto(): ArenaDto {
//            return <ArenaDto>{
//                dim: this.arena.dim
//            };
//        }
//
//        public getWorldDto(pid: number): WorldUpdateDto {
//            return <WorldUpdateDto>{
//                pid: pid,
//                bikes: this.getBikeDtos(),
//                arena: this.getArenaDto()
//            };
//        }
	
	
	
}