import {Vector} from "../util";
import {BikeDto} from "../dto";
export class Bike {

    public pid: number;
    public pos : Vector;
    public spd : Vector;
    public trail: Vector[];

    private spdMagnitude = 0.2;
    public isDead: boolean = false;
    public deathTimestamp: number = null;

    constructor( bikeDto: BikeDto ) {
        this.pid = bikeDto.pid;
        this.pos = bikeDto.pos;
        this.spd = bikeDto.spd;
        this.isDead = bikeDto.isDead !== null ? false : bikeDto.isDead;
        this.deathTimestamp = bikeDto.deathTimestamp;
        this.trail = bikeDto.trail || [bikeDto.pos];
    }

    public getPid() : number {
        return this.pid;
    }

    public setSpeed( spd: Vector ) {
        if ( ( !this.spd.x && !spd.x ) || ( !this.spd.y && !spd.y ) ) {
            return false;
        }
        this.spd = spd;
        this.trail.push(this.pos);
        return true;
    }

    public update() {
        if ( !this.isDead ) {
            this.pos.x += this.spd.x * this.spdMagnitude;
        }
    }

    public kill( timeOfDeath?: number ) {
        this.spd = new Vector(0, 0);
        this.isDead = true;
        this.deathTimestamp = timeOfDeath || Math.floor(Date.now());
    }


}

