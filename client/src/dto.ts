module Dto {
    import Vector = Util.Vector;
    export interface BikeDto {
        pid: number
        pos: Vector,
        spd: Vector,
        crashed: boolean,
        crashing: boolean,
        spectating: boolean,
        deathTimestamp?: number
        trail: Vector[];
    }

    export interface ArenaDto {
        dimensions: Vector
    }

    export interface WorldUpdateDto {
        timestamp: number,
        bikes: BikeDto[],
        arena: ArenaDto
    }

    export interface ClientUpdateDto {
        pid : number;
        xSpd : number;
        ySpd : number;
    }
}
