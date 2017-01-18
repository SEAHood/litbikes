module Model {
    import Vector = Util.Vector;
    import BikeDto = Dto.BikeDto;
    export class Bike {

        private pid: number;
        private pos : Vector;
        private spd : Vector;
        private trail: Vector[];

        private spdMagnitude = 0.4;
        private isDead: boolean = false;
        private deathTimestamp: number = null;

        constructor( bikeDto: BikeDto ) {
            this.pid = bikeDto.pid;
            this.pos = new Vector( bikeDto.pos.x, bikeDto.pos.y );
            this.spd = new Vector( bikeDto.spd.x, bikeDto.spd.y );
            this.isDead = bikeDto.isDead !== null ? false : bikeDto.isDead;
            this.deathTimestamp = bikeDto.deathTimestamp;
            this.trail = bikeDto.trail || [bikeDto.pos];
        }


        public setDirection(spd: Vector ) {
            if ( ( !this.spd.x && !spd.x ) || ( !this.spd.y && !spd.y ) ) {
                return false;
            }
            this.spd = spd;
            this.trail.push(this.pos);
            return true;
        }

        public update() {
            if ( !this.isDead ) {
                let xDiff = this.spd.x * this.spdMagnitude;
                let yDiff = this.spd.y * this.spdMagnitude;
                this.pos = new Vector(this.pos.x + xDiff, this.pos.y + yDiff);
            }
        }

        public updateFromDto( dto : BikeDto ) {
            this.pos = dto.pos;
            this.spd = dto.spd;
            this.trail = dto.trail;
            this.isDead = dto.isDead;
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
            p.ellipse(this.pos.x, this.pos.y, 5, 5);

            p.strokeWeight(2);
            p.stroke( 255 );

            // should sort the trail
            _.each( this.trail, ( tp : Vector, i : number ) => {
                let lastBeforeBike = i >= this.trail.length - 1;
                let nextTp = lastBeforeBike ? this.pos : this.trail[i+1];
                p.line(tp.x, tp.y, nextTp.x, nextTp.y);
                //p.ellipse(tp.x, tp.y, 2, 2);
            });

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

        public getPid() : number {
            return this.pid;
        }
        public getPos() : Vector {
            return this.pos;
        }
        public getSpd() : Vector {
            return this.spd;
        }

    }

}
