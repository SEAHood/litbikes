module Dto {
    import Vector = Util.Vector;
    import Bike = Model.Bike;
    export interface BikeDto {
        pid: number;
        name: string;
        pos: Vector;
        dir: Vector;
        spd: number;
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

    export interface HelloDto {
        gameSettings : GameSettingsDto;
        bike : BikeDto;
        arena : ArenaDto;
        world : WorldUpdateDto;
    }

    export interface GameJoinDto {
        bike : BikeDto;
        scores : ScoreDto[];
    }

    export interface ArenaDto {
        size: number;
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
    
    export interface ClientUpdateDto {
        pid : number;
        xDir : number;
        yDir : number;
        xPos : number;
        yPos : number;
    }

    export interface ClientGameJoinDto {
        name : string;
    }
}
