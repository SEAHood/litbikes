module Dto {
    import Vector = Util.Vector;
    export interface BikeDto {
        pid: number
        pos: Vector,
        spd: Vector,
        isDead: boolean,
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
}
