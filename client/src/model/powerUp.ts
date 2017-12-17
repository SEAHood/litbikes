module Model {
    import Vector = Util.Vector;
    import PowerUpDto = Dto.PowerUpDto;
    export class PowerUp {
        private id: string;
        private pos: Vector;
        private type: string;
        private collected: boolean;
        private animating: boolean;
        private popSize: number;
        
        constructor(_id: string, _pos: Vector, _type: string) {
            this.id = _id;
            this.pos = _pos;
            this.type = _type;
            this.collected = false;
            this.animating = false;
            this.popSize = 0;
        }
    
        public getId(): string {
            return this.id;
        }
            
        public getPos(): Vector {
            return this.pos;
        }
    
        public setPos(pos: Vector) {
            this.pos = pos;
        }
    
        public getType() {
            return this.type;
        }
    
        public setType(type: string) {
            this.type = type;
        }

        public isCollected(): boolean {
            return this.collected;
        }

        public collect() {
            this.collected = true;
            this.animating = true;
        }

        public updateFromDto(dto: PowerUpDto) {
            this.pos = dto.pos;            
            if (!this.collected && dto.collected) {
                this.collect();
            }
        }

        public draw(p: p5) {
            if (!this.collected) {
                let colour = "rgb(255,255,255)";
                switch (this.type.toLowerCase()) {
                    case "rocket":
                        // colour = "rgb(255,153,51)";
                        colour = "rgb(193,253,51)";
                        break;
                    case "slow":
                        //colour = "rgb(243,243,21)";
                        colour = "rgb(252,90,184)";
                        break;
                    default:
                        break;
                }
                p.noStroke();
                p.fill(colour);
                p.ellipse(this.pos.x, this.pos.y, 6, 6);
                // p.fill(colour.replace('%A%', '1'));
                // p.ellipse(this.pos.x, this.pos.y, 4, 4);
            } else if (this.animating) {
                p.fill('rgba(0,0,0,0)');
                p.stroke(255);
                p.strokeWeight(2);
                p.ellipse(this.pos.x, this.pos.y, this.popSize, this.popSize);
                this.popSize = this.popSize + 1.5;
                if (this.popSize > 20) {
                    this.animating = false;
                }
            }
            
            // p.textFont("Courier");
            // p.textSize(15);
            // p.textAlign('center', 'middle');
            //p.text('i: ' + this.id.substr(0, 5) + '\nc: ' + this.collected + '\na: ' + this.animating, this.pos.x, Math.max(0, this.pos.y - 45));
        }
        
    }
}