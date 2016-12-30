
import _ = require('underscore');
import {BikeDto, ArenaDto, WorldUpdateDto} from "../dto";
import {Bike} from "../model/bike";
var ThreadedLock = require("threaded-lock");
import {Arena} from "../model/arena";
import {Vector, Util} from "../util";

//module Server {
    export class GameWorld {
        private fps = 60;
        private pidGen = 0;
        private isRunning = false;

        private bikes: Bike[];
        private arena: Arena;

        constructor() {
            let arenaDimensions = new Vector(500, 500);
            this.arena = new Arena(arenaDimensions);
            this.cycle();
        }

        cycle() {
            if (this.isRunning) {
                // dont console log crap here unless you want lag
                //console.log(new Date());

                // TODO GAME CYCLE
            }

            setTimeout(() => this.cycle(), 1 / this.fps)
        }

        public handleUpdate(dto: BikeDto) {
            let bike = this.getBike(dto.pid);
            if (bike) {
                bike.spd = dto.spd;
            } else {
                this.addBike(new Bike(dto));
            }
        }


        public newBike(): number {
            let newBike = this.createBike();
            this.addBike(newBike);
            return newBike.getPid();
        }

        private createBike(): Bike {
            let pid = this.pidGen++;
            let pos = new Vector(Util.randInt(20, 480), Util.randInt(20, 480));
            let spd = null;
            var direction = Util.randInt(1, 4);
            switch (direction) {
                case 1:
                    spd = new Vector(0, -1);
                    break;
                case 2:
                    spd = new Vector(0, 1);
                    break;
                case 3:
                    spd = new Vector(-1, 0);
                    break;
                case 4:
                    spd = new Vector(1, 0);
                    break;
                default:
                    break;
            }

            let bike = <BikeDto>{
                pid: pid,
                pos: pos,
                spd: spd
            };
            return new Bike(bike);
        }

        public addBike(bike: Bike) {
            this.bikes.push(bike);
        }

        public async removeBike(pid: number) {
            let tl = new ThreadedLock("bikes-lock");
            await tl.lock();
            this.bikes = _.filter(this.bikes, (b: Bike) => b.pid !== pid);
            tl.unlock();
        }

        public getBike(pid: number): Bike {
            return _.find(this.bikes, b => b.pid === pid);
        }

        private getBikeDtos(): BikeDto[] {
            let bikeDtos: BikeDto[] = [];
            _.each(this.bikes, bike => {
                bikeDtos.push(
                    <BikeDto>{
                        pid: bike.pid,
                        pos: bike.pos,
                        spd: bike.spd,
                        isDead: bike.isDead,
                        deathTimestamp: bike.deathTimestamp,
                        trail: bike.trail
                    }
                );
            });
            return bikeDtos;
        }

        private getArenaDto(): ArenaDto {
            return <ArenaDto>{
                dim: this.arena.dim
            };
        }

        public getWorldDto(pid: number): WorldUpdateDto {
            return <WorldUpdateDto>{
                pid: pid,
                bikes: this.getBikeDtos(),
                arena: this.getArenaDto()
            };
        }
    }
//}