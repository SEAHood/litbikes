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
        private isPlayer: boolean;

        constructor(pid: number) {
            this.pid = pid;
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
        private isAlive(): boolean {
            return !this.spectating && !this.crashed;
        }
        private isVisible() : boolean {
            return !this.spectating || this.bike.isCrashing();
        }
        public isSpectating() : boolean {
            return this.spectating;
        }
        
        public update() {
            this.bike.update(this.isAlive());
        }

        public draw( p : p5, showName : boolean ) {
            if (this.isVisible()) {
                this.bike.draw(p, showName);
            }
        }

        public updateFromDto( dto : PlayerDto ) {
            console.log(dto);
            if ( !this.crashed && dto.crashed ) {
                this.bike.crash();
                this.deathTimestamp = dto.deathTimestamp || Math.floor(Date.now());
            }

            if ( this.crashed && !dto.crashed ) {
                this.bike.uncrash();
            }

            this.crashed = dto.crashed;
            this.crashedInto = dto.crashedInto;
            this.crashedIntoName = dto.crashedIntoName;
            this.spectating = dto.spectating;
            this.score = dto.score;

            this.bike.updateFromDto(dto.bike);
            console.log(this);
        }
    }
}