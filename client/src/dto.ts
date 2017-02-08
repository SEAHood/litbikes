module Dto {
    import Vector = Util.Vector;
    import Bike = Model.Bike;
    export interface BikeDto {
        pid: number
        pos: Vector,
        spd: Vector,
        spdMag: number,
        crashed: boolean,
        crashing: boolean,
        crashedInto: string,
        spectating: boolean,
        deathTimestamp?: number
        trail: TrailSegmentDto[];
        colour: string;
    }

    export interface RegistrationDto {
        gameSettings : GameSettingsDto;
        bike : BikeDto;
        arena : ArenaDto;
        world : WorldUpdateDto;
    }

    export interface ArenaDto {
        dimensions: Vector
    }

    export interface TrailSegmentDto {
        start: Vector,
        end: Vector
    }

    export interface WorldUpdateDto {
        timestamp: number,
        gameTick: number,
        bikes: BikeDto[],
        arena: ArenaDto
    }

    export interface GameSettingsDto {
        gameTickMs : number;
    }

    export interface ClientUpdateDto {
        pid : number;
        xSpd : number;
        ySpd : number;
        xPos : number;
        yPos : number;
    }
}
