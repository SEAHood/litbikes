module Dto {
    import Vector = Util.Vector;
    import Bike = Model.Bike;
    export interface BikeDto {
        pid: number;
        name: string;
        pos: Vector;
        spd: Vector;
        spdMag: number;
        crashed: boolean;
        crashing: boolean;
        crashedInto: number;
        crashedIntoName: string;
        spectating: boolean;
        deathTimestamp?: number
        trail: TrailSegmentDto[];
        colour: string; // includes %A% alpha
        score: number;
    }

    export interface RegistrationDto {
        gameSettings : GameSettingsDto;
        bike : BikeDto;
        arena : ArenaDto;
        world : WorldUpdateDto;
        scores : ScoreDto[];
    }

    export interface ArenaDto {
        dimensions: Vector;
    }

    export interface TrailSegmentDto {
        start: Vector;
        end: Vector;
    }

    export interface WorldUpdateDto {
        timestamp: number;
        gameTick: number;
        bikes: BikeDto[];
        arena: ArenaDto;
    }

    export interface ChatMessageDto {
        timestamp: number;
        source: string;
        sourceColour: string; // includes %A% alpha
        message: string;
        isSystemMessage: boolean;
    }

    export interface ScoreDto {
        pid: number;
        name: string;
        score: number;
    }

    export interface GameSettingsDto {
        gameTickMs : number;
    }

    export interface ClientRegistrationDto {
        name : string;
    }
    
    export interface ClientUpdateDto {
        pid : number;
        xSpd : number;
        ySpd : number;
        xPos : number;
        yPos : number;
    }
}
