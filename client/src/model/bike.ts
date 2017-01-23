module Model {
    import Vector = Util.Vector;
    import BikeDto = Dto.BikeDto;
    import NumberUtil = Util.NumberUtil;
    export class Bike {

        private pid: number;
        private pos : Vector;
        private spd : Vector;
        private trail: Vector[];

        private spdMag: number;
        private crashed: boolean = false;
        private crashing: boolean = false;
        private spectating: boolean = false;
        private deathTimestamp: number = null;

        private trailOpacity: number = 1;

        constructor( bikeDto: BikeDto ) {
            this.pid = bikeDto.pid;
            this.pos = new Vector( bikeDto.pos.x, bikeDto.pos.y );
            this.spd = new Vector( bikeDto.spd.x, bikeDto.spd.y );
            this.crashed = bikeDto.crashed;
            this.spectating = bikeDto.spectating;
            this.deathTimestamp = bikeDto.deathTimestamp;
            this.trail = bikeDto.trail || [bikeDto.pos];
            this.spdMag = bikeDto.spdMag;
        }


        public setDirection(spd: Vector ) {
            if ( !this.crashed ) {
                if (( !this.spd.x && !spd.x ) || ( !this.spd.y && !spd.y )) {
                    return false;
                }
                this.spd = spd;
                this.trail.push(this.pos);
                return true;
            }
        }

        public update() {
            if ( this.canMove() ) {
                let xDiff = (this.spd.x * this.spdMag);// * this.timeDilation.x;
                let yDiff = (this.spd.y * this.spdMag);// * this.timeDilation.y;
                this.pos = new Vector(this.pos.x + xDiff, this.pos.y + yDiff);
            }

            if ( this.isCrashing() ) {
                this.trailOpacity = Math.max(this.trailOpacity - 0.02, 0);
                if ( this.trailOpacity == 0 ) {
                    this.crashing = false;
                    this.trailOpacity = 1;
                }
            }
        }

        public updateFromDto( dto : BikeDto ) {
            this.pos = dto.pos;
            this.spd = dto.spd;
            this.spdMag = dto.spdMag;
            this.trail = dto.trail;

            if ( !this.crashed && dto.crashed ) {
                this.crash();
            }

            this.crashed = dto.crashed;
            this.spectating = dto.spectating;
        }

        public crash( timeOfCrash?: number ) {
            this.spd = new Vector(0, 0);
            this.crashed = true;
            this.crashing = true;
            this.deathTimestamp = timeOfCrash || Math.floor(Date.now());
            this.trail.push(this.pos);
        }

        public draw( p : p5 ) {

            if ( this.isVisible() ) {
                p.noStroke();
                p.stroke('rgba(220, 220, 220, ' + this.trailOpacity + ')');
                p.ellipse(this.pos.x, this.pos.y, 5, 5);

                p.strokeWeight(2);
                p.stroke('rgba(220, 220, 220, ' + this.trailOpacity + ')');

                // todo should sort the trail
                _.each( this.trail, ( tp : Vector, i : number ) => {
                    let lastBeforeBike = i >= this.trail.length - 1;
                    let nextTp = lastBeforeBike ? this.pos : this.trail[i+1];
                    p.line(tp.x, tp.y, nextTp.x, nextTp.y);
                    //p.ellipse(tp.x, tp.y, 2, 2);
                });

                if ( this.isCrashing() ) {
                    // Explosion
                    //p.fill('rgba(' + NumberUtil.rand255() +', 0, 0, 0.50)');
                    p.fill('rgba(0, ' +', ' + NumberUtil.rand255() + ', 0, 0.50)');
                    p.ellipse(this.pos.x, this.pos.y, 20, 20);

                    var randCol = NumberUtil.rand255();
                    //p.fill('rgba(' + randCol +', ' + randCol + ', 0, 0.50)');
                    p.fill('rgba(0, ' + randCol + ', 0, 0.50)');
                    var randSize = Math.floor(Math.random() * 40);
                    p.ellipse(this.pos.x, this.pos.y, randSize, randSize);
                }
            }
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

        public isCrashed() : boolean {
            return this.crashed;
        }
        public isCrashing() : boolean {
            return this.crashing;
        }

        private isVisible() : boolean {
            return !this.spectating || this.crashing
        }

        public isSpectating() : boolean {
            return this.spectating;
        }

        public canMove() : boolean {
            return !this.isCrashed() && !this.isSpectating();
        }
    }

}
