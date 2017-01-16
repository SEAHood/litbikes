
module LitBikes.Util {
    import WorldUpdateDto = LitBikes.Dto.WorldUpdateDto;
    export class Vector {
        public x: number;
        public y: number;
        constructor(x: number, y: number) {
            this.x = x;
            this.y = y;
        }
    }

    export class Util {
        public static randInt(min, max) {
            return Math.floor(Math.random() * (max - min + 1)) + min;
        }
    }

    export class Connection {
        public socket : any;
        public pid : number;

        constructor( socket : any, pid : number ) {
            this.socket = socket;
            this.pid = pid;
        }

        public fireWorldUpdated( worldUpdate : WorldUpdateDto ) {
            this.socket.emit('world-update', worldUpdate);
        }
    }
}

