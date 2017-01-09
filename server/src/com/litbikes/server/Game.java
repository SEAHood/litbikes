package com.litbikes.server;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;
import com.litbikes.util.Vector;

public class Game {
	private int pidGen = 0;
	private boolean isRunning = false;
	private static final double FPS = 60.0;
	
	private List<Bike> bikes;
	private Arena arena;
	
	public Game() {
		Vector arenaDim = new Vector(500, 500);
		this.arena = new Arena(arenaDim);
	}

	public static Game create() {
		return new Game();
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
	    }
	}
	
	public void newPlayer() {		
		Bike newBike = Bike.create(this.pidGen++);
		this.bikes.add( newBike );
	}
	
	public boolean handleClientUpdate(ClientUpdateDto data) {
		if ( data.isValid() ) {
			return true;
		} else 
			return false;
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