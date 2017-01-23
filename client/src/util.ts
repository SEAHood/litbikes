
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

        /* accepts parameters
         * h  Object = {h:x, s:y, v:z}
         * OR
         * h, s, v
         */
        HSVtoRGB(h, s, v) {
            var r, g, b, i, f, p, q, t;
            if (arguments.length === 1) {
                s = h.s, v = h.v, h = h.h;
            }
            i = Math.floor(h * 6);
            f = h * 6 - i;
            p = v * (1 - s);
            q = v * (1 - f * s);
            t = v * (1 - (1 - f) * s);
            switch (i % 6) {
                case 0: r = v, g = t, b = p; break;
                case 1: r = q, g = v, b = p; break;
                case 2: r = p, g = v, b = t; break;
                case 3: r = p, g = q, b = v; break;
                case 4: r = t, g = p, b = v; break;
                case 5: r = v, g = p, b = q; break;
            }
            return {
                r: Math.round(r * 255),
                g: Math.round(g * 255),
                b: Math.round(b * 255)
            };
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

