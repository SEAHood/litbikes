module Model {
    import Vector = Util.Vector;
    import BikeDto = Dto.BikeDto;
    import NumberUtil = Util.NumberUtil;
    import TrailSegmentDto = Dto.TrailSegmentDto;
    import ColourUtil = Util.ColourUtil;
    export class Bike {

        private pid: number;
        private pos : Vector;
        private spd : Vector;
        private trail: TrailSegment[];

        private spdMag: number;
        private crashed: boolean = false;
        private crashing: boolean = false;
        private spectating: boolean = false;
        private deathTimestamp: number = null;
        private crashedInto: string;

        private colour: string;
        private trailOpacity: number = 1;


        constructor( bikeDto: BikeDto ) {
            this.pid = bikeDto.pid;
            this.pos = new Vector( bikeDto.pos.x, bikeDto.pos.y );
            this.spd = new Vector( bikeDto.spd.x, bikeDto.spd.y );
            this.crashed = bikeDto.crashed;
            this.spectating = bikeDto.spectating;
            this.deathTimestamp = bikeDto.deathTimestamp;
            this.spdMag = bikeDto.spdMag;
            this.colour = bikeDto.colour;
            this.crashedInto = bikeDto.crashedInto;

            this.trail = [];
            _.each(bikeDto.trail, (seg : TrailSegmentDto) => {
                this.trail.push( TrailSegment.fromDto(seg) );
            });

        }


        public setDirection(spd: Vector ) {
            if ( !this.crashed ) {
                if (( !this.spd.x && !spd.x ) || ( !this.spd.y && !spd.y )) {
                    return false;
                }
                this.spd = spd;
                this.addTrailSegment();
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
            this.trail = [];
            this.colour = dto.colour;
            _.each(dto.trail, (seg : TrailSegmentDto) => {
                this.trail.push( TrailSegment.fromDto(seg) );
            });

            if ( !this.crashed && dto.crashed ) {
                this.crash();
            }

            if ( this.crashed && !dto.crashed ) {
                // probs respawned
                // TODO: have the server send this info instead
                this.crashing = false;
                this.trailOpacity = 1;
            }

            this.crashed = dto.crashed;
            this.crashedInto = dto.crashedInto;
            this.spectating = dto.spectating;
        }

        private addTrailSegment() {
            let lastSeg : Vector = _.last(this.trail).end;
            let newSeg = new TrailSegment(
                new Vector(lastSeg.x, lastSeg.y),
                new Vector(this.pos.x, this.pos.y)
            );
            this.trail.push(newSeg);
        }

        public crash( timeOfCrash?: number ) {
            this.spd = new Vector(0, 0);
            this.crashed = true;
            this.crashing = true;
            this.deathTimestamp = timeOfCrash || Math.floor(Date.now());
            this.addTrailSegment();
        }

        public draw( p : p5 ) {

            if ( this.isVisible() ) {

                // Draw bike
                p.noStroke();
                p.fill(this.colour.replace('%A%', '1'));
                p.ellipse(this.pos.x, this.pos.y, 5, 5);


                // Draw trail
                p.strokeWeight(2);
                p.stroke(this.colour.replace('%A%', this.trailOpacity.toString()));
                let lastSeg : Vector = _.last(this.trail).end;
                let newSeg = new TrailSegment(
                    new Vector(lastSeg.x, lastSeg.y),
                    new Vector(this.pos.x, this.pos.y)
                );

                let trail = _.clone(this.trail);
                trail.push(newSeg);

                _.each( trail, ( tp : TrailSegment ) => {
                    p.line(tp.start.x, tp.start.y, tp.end.x, tp.end.y);
                });

                // Draw crashing
                if ( this.isCrashing() ) {
                    // Explosion
                    var randColour = NumberUtil.rand255();
                    p.stroke('rgba(' + randColour + ', 0, 0, 0.80)');
                    p.fill('rgba(' + randColour + ', ' + randColour + ' , 0, 0.75)');
                    p.ellipse(this.pos.x, this.pos.y, 20, 20);

                    var randSize = Math.floor(Math.random() * 35);
                    randColour = NumberUtil.rand255();
                    p.stroke('rgba(' + randColour + ', ' + randColour + ' , 0, 0.55)');
                    p.fill('rgba(' + NumberUtil.rand255() + ', 0, 0, 0.65)');
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
        public getColour() : String {
            return this.colour;
        }
        public isCrashed() : boolean {
            return this.crashed;
        }
        public isCrashing() : boolean {
            return this.crashing;
        }

        public getCrashedInto() : string {
            return this.crashedInto;
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
