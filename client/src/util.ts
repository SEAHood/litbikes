
module Util {
    import WorldUpdateDto = Dto.WorldUpdateDto;
    export class Vector {
        public x: number;
        public y: number;
        constructor(x: number, y: number) {
            this.x = x;
            this.y = y;
        }

        /*constructor(obj: any) {
            if ( !!obj.x && !!obj.y ) {
                this.x = obj.x;
                this.y = obj.y;
            }
        }

        public add(x: number, y: number) {
            this.x += x;
            this.y += y;
        }*/

    }

    export class NumberUtil {
        public static randInt(min, max) : number {
            return Math.floor(Math.random() * (max - min + 1)) + min;
        }

        public static rand255() : number {
            return Math.floor(Math.random() * 256);
        }
    }

    export class ColourUtil {
        public static rgba(r,g,b,a) : string {
            return 'rgba('+r+','+g+','+b+','+a+')';
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

