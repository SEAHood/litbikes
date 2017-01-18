
module Game {

    import Bike = Model.Bike;
    import Arena = Model.Arena;
    import WorldUpdateDto = Dto.WorldUpdateDto;
    import BikeDto = Dto.BikeDto;
    import Vector = Util.Vector;
    import ClientUpdateDto = Dto.ClientUpdateDto;
    export class Game {
        private player : Bike;
        private ai;
        private bg;
        private gameWidth;
        private gameHeight;
        private host = 'http://fresh.crabdance.com:9092';
        private socket = io.connect(this.host);
        private arena : Arena;
        private bikes : Bike[] = []; //All bikes other than your own
        private trailPoints = [];
        private registered = false;
        private version = 0.1;
        private gameStarted = false;

        private p5Instance : p5;

        constructor() {
            this.socket.on('client-registered', ( data : BikeDto ) => {
                console.log("Registered as bike " + data.pid);

                this.player = new Bike(data);

                this.socket.emit('request-world-update');
            });

            setInterval(() => this.socket.emit('request-world-update'), 1000);

            this.socket.on('world-update', ( data : WorldUpdateDto ) => {
                let updatedBikes = _.pluck(data.bikes, 'pid');
                let existingBikes = _.pluck(this.bikes, 'pid');
                _.each( existingBikes, ( pid : number ) => {
                    if ( !_.contains(updatedBikes, pid ) ) {
                        this.bikes = _.reject(this.bikes, ( b : Bike ) => b.getPid() === pid );
                    }
                });

                _.each( data.bikes, ( b : BikeDto ) => {
                    if ( b.pid === this.player.getPid() ) {
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

                if ( !this.gameStarted ) {
                    this.arena = new Arena( data.arena.dimensions );
                    this.p5Instance = new p5(this.sketch(), this.ui.game);
                    this.gameStarted = true;
                }
            });

            $(document).on('keydown', ev => {
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
                    //socket.emit('reset');
                }

                if ( newVector ) {
                    this.player.setDirection(newVector);
                    this.sendClientUpdate();
                }
            });

            console.log("Sent registration packet to " + this.host);
            this.socket.emit('register');
        }

        private sendClientUpdate() {
            let updateDto : ClientUpdateDto = {
                pid : this.player.getPid(),
                xSpd : this.player.getSpd().x,
                ySpd : this.player.getSpd().y
            };
            this.socket.emit('client-update', updateDto);
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
            this.player.update();
            this.arena.draw(p);

            p.fill(255);
            p.textAlign('left', 'top')
            p.text("LitBikes 0.0", 10, 10);
            p.text(
                "pid: " + this.player.getPid() + "\n" +
                "pos: " + this.player.getPos().x.toFixed(0) + ", " + this.player.getPos().y.toFixed(0) + "\n" +
                "spd: "+ this.player.getSpd().x + ", " + this.player.getSpd().y
            , 10, 30, 300, 500);

            _.each( this.bikes, ( b : Bike ) => {
                b.update();
                b.draw(p);
            });

            this.player.draw(p);
        }

        private setup( p : p5 ) {
            p.createCanvas(this.arena.dimensions.x, this.arena.dimensions.y);
            this.arena.draw(p);

            //bg = new Background();
            //setupSocketListeners();
        }

    }
    new Game();


}