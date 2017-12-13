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
        private isPlayer: boolean;

        private colour: string;
        private trailOpacity: number;

        private idRingBlinkTime: number;
        private idRingBlinkOn: boolean;

        constructor( bikeDto: BikeDto, isPlayer: boolean ) {
            this.setPos(new Vector( bikeDto.pos.x, bikeDto.pos.y ));
            this.dir = new Vector( bikeDto.dir.x, bikeDto.dir.y );
            this.spd = bikeDto.spd;
            this.colour = bikeDto.colour;
            this.isPlayer = isPlayer;

            this.trail = [];
            _.each(bikeDto.trail, (seg : TrailSegmentDto) => {
                this.trail.push( TrailSegment.fromDto(seg) );
            });

            this.trailOpacity = 1;
            this.idRingBlinkTime = -1;
            this.idRingBlinkOn = false;
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
            this.setPos(dto.pos);
            this.dir = dto.dir;
            this.spd = dto.spd;
            this.trail = [];
            this.colour = dto.colour;
            _.each(dto.trail, (seg : TrailSegmentDto) => {
                this.trail.push( TrailSegment.fromDto(seg) );
            });

            if ( this.respawning && Date.now() - 2100 > this.lastRespawn ) {
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
            console.log("Crash!");
            this.dir = new Vector(0, 0);
            this.crashing = true;
            this.addTrailSegment();
        }

        public uncrash( timeOfCrash?: number ) {
            this.respawning = true;
            this.crashing = false;
            this.trailOpacity = 1;
            this.lastRespawn = Date.now();
            this.respawning = true;
        }

        public draw( p : p5, showName : boolean ) {
            // Respawning effect
            if ( this.respawning && this.isPlayer ) {
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
                p.text("not workin soz", this.pos.x, Math.max(0, this.pos.y - 15));
            }

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
