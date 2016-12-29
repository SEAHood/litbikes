export interface BikeDto {
    pid: number
    x: number,
    y: number,
    xspeed: number,
    yspeed: number,
    isDead: boolean,
    deathTimestamp?: number
    trail: TrailDto;
}

export interface TrailDto {
    points: TrailPointDto[]
}

export interface TrailPointDto {
    x: number,
    y: number
}