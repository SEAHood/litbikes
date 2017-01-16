module Model {
    import Vector = LitBikes.Util.Vector;
    import BikeDto = LitBikes.Dto.BikeDto;
    export class Bike {

        public pid: number;
        public pos : Vector;
        public spd : Vector;
        public trail: Vector[];

        private spdMagnitude = 0.2;
        public isDead: boolean = false;
        public deathTimestamp: number = null;

        constructor( bikeDto: BikeDto ) {
            this.pid = bikeDto.pid;
            this.pos = bikeDto.pos;
            this.spd = bikeDto.spd;
            this.isDead = bikeDto.isDead !== null ? false : bikeDto.isDead;
            this.deathTimestamp = bikeDto.deathTimestamp;
            this.trail = bikeDto.trail || [bikeDto.pos];
        }

        public getPid() : number {
            return this.pid;
        }

        public setSpeed( spd: Vector ) {
            if ( ( !this.spd.x && !spd.x ) || ( !this.spd.y && !spd.y ) ) {
                return false;
            }
            this.spd = spd;
            this.trail.push(this.pos);
            return true;
        }

        public update() {
            if ( !this.isDead ) {
                this.pos.x += this.spd.x * this.spdMagnitude;
            }
        }

        public kill( timeOfDeath?: number ) {
            this.spd = new Vector(0, 0);
            this.isDead = true;
            this.deathTimestamp = timeOfDeath || Math.floor(Date.now());
        }

        public draw( p : p5 ) {
            p.noStroke();
            //p.fill('rgba(' + rand255() +', 0, 0, 0.50)');
            p.fill(230);
            p.ellipse(this.pos.x, this.pos.y, 20, 20);

            /*if ( !this.isDead ) {
                p.fill( this.colour );
                p.ellipse(this.x, this.y, 5, 5);
            } else if ( !this.explosionEnded ) {
                if ( Math.floor(Date.now()) - this.deathTimestamp > this.explosionTime ) {
                    this.explosionEnded = true;
                    return;
                }

                // Explosion
                p.fill('rgba(' + rand255() +', 0, 0, 0.50)');
                p.ellipse(this.x, this.y, 20, 20);

                var randCol = rand255();
                p.fill('rgba(' + randCol +', ' + randCol + ', 0, 0.50)');
                var randSize = Math.floor(Math.random() * 40);
                p.ellipse(this.x, this.y, randSize, randSize);
            }*/
        }


    }


}
