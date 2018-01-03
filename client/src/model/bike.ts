module Model {
    import Vector = Util.Vector;
    import BikeDto = Dto.BikeDto;
    import NumberUtil = Util.NumberUtil;
    import TrailSegmentDto = Dto.TrailSegmentDto;
    import ColourUtil = Util.ColourUtil;
    export class Bike {
        private pos : Vector;
        private dir : Vector;
        private trail: TrailSegment[];

        private spd: number;
        private crashing: boolean;
        private respawning: boolean;
        private lastRespawn: number; // Respawn timestamp

        private colour: string;
        private trailOpacity: number;

        private idRingBlinkTime: number;
        private idRingBlinkOn: boolean;
        private idRingSize: number;
        private idRingPulseCount = 0;
        private idRingPulseMax = 2;
        private idRingDuration = 1500; //ms

        constructor(bikeDto: BikeDto) {
            this.setPos(new Vector(bikeDto.pos.x, bikeDto.pos.y));
            this.dir = new Vector(bikeDto.dir.x, bikeDto.dir.y);
            this.spd = bikeDto.spd;
            this.colour = bikeDto.colour;

            this.trail = [];
            _.each(bikeDto.trail, (seg : TrailSegmentDto) => {
                this.trail.push(TrailSegment.fromDto(seg));
            });

            this.trailOpacity = 1;
            this.idRingBlinkTime = -1;
            this.idRingBlinkOn = false;
            this.idRingSize = 0;
            this.respawning = true;
        }

        public update(canMove: boolean) {
            if (canMove) {
                let xDiff = this.dir.x * this.spd;// * this.timeDilation.x;
                let yDiff = this.dir.y * this.spd;// * this.timeDilation.y;
                this.setPos(new Vector(this.pos.x + xDiff, this.pos.y + yDiff));
            }

            if (this.crashing) {
                this.trailOpacity = Math.max(this.trailOpacity - 0.02, 0);
                if (this.trailOpacity == 0) {
                    this.crashing = false;
                    this.trailOpacity = 1;
                }
            }
        }

        public updateFromDto( dto : BikeDto ) {
            this.pos = dto.pos;
            this.dir = dto.dir;
            this.spd = dto.spd;
            this.colour = dto.colour;
            this.trail = [];
            _.each(dto.trail, (seg : TrailSegmentDto) => {
                this.trail.push( TrailSegment.fromDto(seg) );
            });

            if ( this.respawning && Date.now() - this.idRingDuration > this.lastRespawn ) {
                this.respawning = false;
            }
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
            this.dir = new Vector(0, 0);
            this.crashing = true;
            this.addTrailSegment();
        }

        public respawned( timeOfCrash?: number ) {
            this.respawning = true;
            this.crashing = false;
            this.trailOpacity = 1;
            this.lastRespawn = Date.now();
        }

        public draw(p: p5, showRespawnRing: boolean, isControlledPlayer: boolean) {
            // Respawning effect
            if (this.respawning && showRespawnRing) {
                let innerRingSize = Math.max(0, this.idRingSize - 10);
                p.fill('rgba(0,0,0,0)');
                p.stroke(255);
                p.strokeWeight(2);
                p.ellipse(this.pos.x, this.pos.y, this.idRingSize, this.idRingSize);
                p.stroke(this.colour.replace('%A%', '1'));
                p.strokeWeight(1);
                p.ellipse(this.pos.x, this.pos.y, innerRingSize, innerRingSize);
                this.idRingSize = this.idRingSize + 1.5;
                if (this.idRingSize > 50) {
                    this.idRingSize = 0;
                    this.idRingPulseCount++;
                    if (this.idRingPulseCount >= this.idRingPulseMax) {
                        this.respawning = false;
                        this.idRingPulseCount = 0;
                    }
                }
            }
            
            // Draw trail
            p.strokeWeight(2);
            p.stroke(this.colour.replace('%A%', this.trailOpacity.toString()));

            // Create trail segment between bike and last trail end
            let headEnd : Vector = _.find(this.trail, t => t.isHead).end;
            let newSeg = new TrailSegment(
                new Vector(headEnd.x, headEnd.y),
                new Vector(this.pos.x, this.pos.y)
            );
            let trail = _.clone(this.trail);
            trail.push(newSeg);

            p.noFill();
            _.each( trail, ( tp : TrailSegment ) => {
                // if (tp.isHead) {
                //     p.stroke('rgb(255,0,0)');
                // } else {
                //     p.stroke(this.colour.replace('%A%', this.trailOpacity.toString()));
                // }
                //p.ellipse(tp.start.x, tp.start.y, 3, 3);
                p.line(tp.start.x, tp.start.y, tp.end.x, tp.end.y);
            });

            // Draw bike
            let bikeColour = isControlledPlayer
                ? "rgb(255, 255, 255)"
                :  this.colour.replace('%A%', '1');
            p.noStroke();
            p.fill(bikeColour);
            p.ellipse(this.pos.x, this.pos.y, 5, 5);

            // Draw crashing
            if ( this.crashing ) {
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

        public getPos() : Vector {
            return this.pos;
        }
        public setPos( pos : Vector ) {
            this.pos = pos;
        }
        public getDir() : Vector {
            return this.dir;
        }
        public getSpd() : number {
            return this.spd;
        }
        public setSpd( spd : number ) {
            this.spd = spd;
        }
        public getColour() : String {
            return this.colour;
        }
        public isCrashing() : boolean {
            return this.crashing;
        }
        public isRespawning() : boolean {
            return this.respawning;
        }
    }
}