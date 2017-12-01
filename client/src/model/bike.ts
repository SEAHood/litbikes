module Model {
    import Vector = Util.Vector;
    import BikeDto = Dto.BikeDto;
    import NumberUtil = Util.NumberUtil;
    import TrailSegmentDto = Dto.TrailSegmentDto;
    import ColourUtil = Util.ColourUtil;
    export class Bike {
        private pid: number;
        private name: string;
        private pos : Vector;
        private spd : Vector;
        private trail: TrailSegment[];

        private spdMag: number;
        private crashed: boolean;
        private crashing: boolean;
        private respawning: boolean;
        private spectating: boolean;
        private deathTimestamp: number;
        private crashedInto: number; // pid last crashed into
        private crashedIntoName: string;
        private score: number;
        private isPlayer: boolean;

        private lastRespawn: number; // Respawn timestamp

        private colour: string;
        private trailOpacity: number;

        private idRingBlinkTime: number;
        private idRingBlinkOn: boolean;

        constructor( bikeDto: BikeDto, isPlayer: boolean ) {
            this.pid = bikeDto.pid;
            this.name = bikeDto.name;
            this.setPos(new Vector( bikeDto.pos.x, bikeDto.pos.y ));
            this.spd = new Vector( bikeDto.spd.x, bikeDto.spd.y );
            this.crashed = bikeDto.crashed;
            this.spectating = bikeDto.spectating;
            this.deathTimestamp = bikeDto.deathTimestamp;
            this.spdMag = bikeDto.spdMag;
            this.colour = bikeDto.colour;
            this.crashedInto = bikeDto.crashedInto;
            this.crashedIntoName = bikeDto.crashedIntoName;
            this.score = bikeDto.score;
            this.isPlayer = isPlayer;

            this.trail = [];
            _.each(bikeDto.trail, (seg : TrailSegmentDto) => {
                this.trail.push( TrailSegment.fromDto(seg) );
            });

            this.lastRespawn = Date.now();
            this.respawning = true;
            this.trailOpacity = 1;
            this.idRingBlinkTime = -1;
            this.idRingBlinkOn = false;
        }

        public update() {
            if ( this.canMove() ) {
                let xDiff = this.spd.x * this.spdMag;// * this.timeDilation.x;
                let yDiff = this.spd.y * this.spdMag;// * this.timeDilation.y;
                this.setPos(new Vector(this.pos.x + xDiff, this.pos.y + yDiff));
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
            this.setPos(dto.pos);

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
                this.lastRespawn = Date.now();
                this.respawning = true;
                this.crashing = false;
                this.trailOpacity = 1;
            }

            if ( this.respawning && Date.now() - 2100 > this.lastRespawn ) {
                this.respawning = false;
            }

            this.crashed = dto.crashed;
            this.crashedInto = dto.crashedInto;
            this.crashedIntoName = dto.crashedIntoName;
            this.spectating = dto.spectating;
            this.score = dto.score;
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

        public draw( p : p5, identify : boolean, showName : boolean ) {

            if ( this.isVisible() ) {

                if ( identify ) {
                    if (this.idRingBlinkOn) {
                        p.fill('rgba(0,0,0,0)');
                        p.strokeWeight(2);
                        p.stroke(255);
                        p.ellipse(this.pos.x, this.pos.y, 50, 50);
                        p.ellipse(this.pos.x, this.pos.y, 20, 20);
                    }

                    if (Date.now() - 150 > this.idRingBlinkTime) {
                        this.idRingBlinkTime = Date.now();
                        this.idRingBlinkOn = !this.idRingBlinkOn;
                    }
                }
                
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

                p.noFill();
                p.beginShape();
                _.each( trail, ( tp : TrailSegment ) => {
                    p.vertex( tp.start.x, tp.start.y);
                });
                p.vertex( this.pos.x, this.pos.y);
                p.endShape();

                // Draw bike
                let bikeColour = this.isPlayer 
                    ? "rgb(255, 255, 255)"
                    :  this.colour.replace('%A%', '1');
                p.noStroke();
                p.fill(bikeColour);
                p.ellipse(this.pos.x, this.pos.y, 5, 5);
                             
                if (showName) {
                    p.textSize(15);
                    p.textAlign('center', 'middle');
                    p.text(this.name, this.pos.x, Math.max(0, this.pos.y - 15));
                }

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
        public getName() : string {
            return this.name;
        }
        public getPos() : Vector {
            return this.pos;
        }
        public setPos( pos : Vector ) {
            this.pos = pos;
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
        public isRespawning() : boolean {
            return this.respawning;
        }
        public getCrashedInto() : number {
            return this.crashedInto;
        }
        public getCrashedIntoName() : string {
            return this.crashedIntoName;
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
