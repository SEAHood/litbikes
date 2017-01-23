
module Game {

    import Bike = Model.Bike;
    import Arena = Model.Arena;
    import WorldUpdateDto = Dto.WorldUpdateDto;
    import BikeDto = Dto.BikeDto;
    import Vector = Util.Vector;
    import ClientUpdateDto = Dto.ClientUpdateDto;
    import RegistrationDto = Dto.RegistrationDto;
    export class Game {
        private player : Bike;
        private host = 'http://fresh.crabdance.com:9092';
        private socket = io.connect(this.host);
        private arena : Arena;
        private bikes : Bike[] = []; //All bikes other than your own
        private registered = false;
        private version ="0.1b";
        private gameStarted = false;
        private gameTickMs : number;
        private p5Instance : p5;

        constructor() {
            this.socket.on('registered', ( data : RegistrationDto ) => {
                this.startGame(data);
            });

            setInterval(() => this.socket.emit('request-world'), 1000);

            this.socket.on('world-update', ( data : WorldUpdateDto ) => {
                if ( this.gameStarted ) {
                    this.processWorldUpdate(data);
                }
            });

            $(document).on('keydown', ev => {
                if ( this.player ) {
                    enum Keys {
                        LEFT_ARROW = 37,
                        UP_ARROW = 38,
                        RIGHT_ARROW = 39,
                        DOWN_ARROW = 40
                    }

                    let keyCode = ev.which;
                    let newVector = null;

                    if (keyCode === Keys.UP_ARROW) {
                        newVector = new Vector(0, -1);
                    } else if (keyCode === Keys.DOWN_ARROW) {
                        newVector = new Vector(0, 1);
                    } else if (keyCode === Keys.RIGHT_ARROW) {
                        newVector = new Vector(1, 0);
                    } else if (keyCode === Keys.LEFT_ARROW) {
                        newVector = new Vector(-1, 0);
                    } else if (keyCode === 82) {
                        this.socket.emit('request-respawn');
                    }

                    if ( newVector ) {
                        this.player.setDirection(newVector);
                        this.sendClientUpdate();
                    }
                }
            });

            console.log("Sent registration packet to " + this.host);
            this.socket.emit('register');
        }


        private startGame( data : RegistrationDto ) {
            if ( !data.gameSettings.gameTickMs ) {
                console.error("Cannot start game - game tick interval is not defined");
            }

            this.player = new Bike( data.bike );
            this.arena = new Arena( data.world.arena );
            this.gameTickMs = data.gameSettings.gameTickMs;

            console.log("Game started at " + this.gameTickMs + "ms per tick");

            this.processWorldUpdate(data.world);

            this.p5Instance = new p5(this.sketch(), this.ui.game);
            this.gameStarted = true;

            setInterval(() => {
                //console.log(new Date().getMilliseconds());
                this.player.update();
                _.each( this.bikes, ( b : Bike ) => {
                    b.update();
                });
            }, this.gameTickMs);
        }

        private processWorldUpdate( data : WorldUpdateDto ) {
            let updatedBikes = _.pluck(data.bikes, 'pid');
            let existingBikes = _.pluck(this.bikes, 'pid');
            _.each( existingBikes, ( pid : number ) => {
                if ( !_.contains(updatedBikes, pid ) ) {
                    this.bikes = _.reject(this.bikes, ( b : Bike ) => b.getPid() === pid );
                }
            });

            _.each( data.bikes, ( b : BikeDto ) => {
                if ( b.pid === this.player.getPid() && this.player ) {
                    this.player.updateFromDto(b);
                } else {
                    let bike = _.find(this.bikes, (bike:Bike) => bike.getPid() === b.pid);
                    if ( bike ) {
                        bike.updateFromDto(b);
                    } else {
                        this.bikes.push(new Bike(b));
                    }
                }
            });
        }

        private sendClientUpdate() {
            let updateDto : ClientUpdateDto = {
                pid : this.player.getPid(),
                xSpd : this.player.getSpd().x,
                ySpd : this.player.getSpd().y
            };
            this.socket.emit('update', updateDto);
        }

        private ui = {
            game : '#game'
        };

        private sketch() {
            return ( p : p5 ) => {
                p.setup = () => this.setup(p);
                p.draw = () => this.draw(p);
            }
        }

        private draw( p : p5 ) {
            //this.player.update();
            this.arena.draw(p);

            p.fill(255);
            p.textAlign('left', 'top')
            p.text("LitBikes " + this.version, 10, 10);
            p.text(
                "fps: " + p.frameRate().toFixed(2) + "\n" +
                "pid: " + this.player.getPid() + "\n" +
                "pos: " + this.player.getPos().x.toFixed(0) + ", " + this.player.getPos().y.toFixed(0) + "\n" +
                "spd: "+ this.player.getSpd().x + ", " + this.player.getSpd().y + "\n" +
                "crashed: " + (this.player.isCrashed() ? "yes" : "no") + "\n" +
                "crashing: " + (this.player.isCrashing() ? "yes" : "no") + "\n" +
                "spectating: " + (this.player.isSpectating() ? "yes" : "no") + "\n"
            , 10, 30, 300, 500);

            _.each( this.bikes, ( b : Bike ) => {
                //b.update();
                b.draw(p);
            });

            this.player.draw(p);
        }

        private setup( p : p5 ) {
            //p.frameRate(30);
            p.createCanvas(this.arena.dimensions.x, this.arena.dimensions.y);
            this.arena.draw(p);

            //bg = new Background();
            //setupSocketListeners();
        }

    }
    new Game();


}