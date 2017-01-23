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
        spectating: boolean,
        deathTimestamp?: number
        trail: Vector[];
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

    export interface WorldUpdateDto {
        timestamp: number,
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
    }
}
