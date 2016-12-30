import {Vector} from "./util";
export interface BikeDto {
    pid: number
    pos: Vector,
    spd: Vector,
    isDead: boolean,
    deathTimestamp?: number
    trail: Vector[];
}

export interface ArenaDto {
    dim: Vector
}

export interface WorldUpdateDto {
    pid: number, //pid of world update target - enforces pid on client
    bikes: BikeDto[],
    arena: ArenaDto
}