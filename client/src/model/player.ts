module Model {
    import PlayerDto = Dto.PlayerDto;
    export class Player {        
        private pid: number;
        private name: string;
        private bike: Bike;
        private crashed: boolean;
        private spectating: boolean;
        private deathTimestamp: number;
        private crashedInto: number; // pid last crashed into
        private crashedIntoName: string;
        private score: number;
        private isControlledPlayer: boolean;
        private currentPowerUp: string;
        private effect: string;

        constructor(pid: number, name: string, bike: Bike, crashed: boolean, 
            spectating: boolean, deathTimestamp: number, crashedInto: number, 
            crashedIntoName: string, score: number, isControlledPlayer: boolean) {
            this.pid = pid;
            this.name = name;
            this.bike = bike;
            this.crashed = crashed;
            this.spectating = spectating;
            this.deathTimestamp = deathTimestamp;
            this.crashedInto = crashedInto;
            this.crashedIntoName  = crashedIntoName;
            this.score = score;
            this.isControlledPlayer = isControlledPlayer;
            
            if (this.isAlive) {
                this.bike.respawned();
            }
        }

        public getPid(): number {
            return this.pid;
        }
        public getName(): string {
            return this.name;
        }
        public setName(name: string) {
            this.name = name;
        }
        public getBike(): Bike {
            return this.bike;
        }
        public setBike(bike: Bike) {
            this.bike = bike;
        }
        public isCrashed() : boolean {
            return this.crashed;
        }
        public getCrashedInto() : number {
            return this.crashedInto;
        }
        public getCrashedIntoName() : string {
            return this.crashedIntoName;
        }
        public isAlive(): boolean {
            return !this.spectating && !this.crashed;
        }
        private isVisible() : boolean {
            return !this.spectating || this.bike.isCrashing();
        }
        public isSpectating() : boolean {
            return this.spectating;
        }
        public getCurrentPowerUp(): string {
            return this.currentPowerUp;
        }
        public getEffect(): string {
            return this.effect || "none";
        }

        public update() {
            this.bike.update(this.isAlive());
        }

        public draw(p: p5, showName: boolean) {
            if (this.isVisible()) {
                let showRespawnRing = this.isAlive() && this.isControlledPlayer;
                this.bike.draw(p, showRespawnRing, this.isControlledPlayer);
                
                if (showName) {
                    p.textSize(15);
                    p.textAlign('center', 'middle');
                    p.text(this.name, this.bike.getPos().x, Math.max(0, this.bike.getPos().y - 15));
                }
                
                //p.text(this.effect ? this.effect : "none", this.bike.getPos().x, Math.max(0, this.bike.getPos().y - 15));

            }
        }

        public updateFromDto( dto : PlayerDto ) {
            let wasAlive = this.isAlive();

            if ( !this.crashed && dto.crashed ) {
                this.bike.crash();
                this.deathTimestamp = dto.deathTimestamp || Math.floor(Date.now());
            }

            if (!dto.crashed && (this.crashed || (this.spectating && !dto.spectating))) {
                this.bike.respawned();
            }

            let oldPowerUp = this.currentPowerUp;            
            this.crashed = dto.crashed;
            this.crashedInto = dto.crashedInto;
            this.crashedIntoName = dto.crashedIntoName;
            this.spectating = dto.spectating;
            this.score = dto.score;
            this.currentPowerUp = dto.currentPowerUp ? dto.currentPowerUp.toLowerCase() : null;
            this.effect = dto.effect;
            this.bike.updateFromDto(dto.bike);
          
        }
    }
}