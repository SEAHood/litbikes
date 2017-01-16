/// <reference path="../../typings/globals/socket.io-client/index.d.ts" />
/// <reference path="../../typings/globals/underscore/index.d.ts" />
/// <reference path="../../typings/p5.d.ts" />

module LitBikes {

    import Bike = Model.Bike;
    import Arena = Model.Arena;
    import WorldUpdateDto = LitBikes.Dto.WorldUpdateDto;
    export class Game {
        private player;
        private ai;
        private bg;
        private gameWidth;
        private gameHeight;
        private host = 'http://localhost:9092';
        private socket = io.connect(this.host);
        private arena : Arena;
        private bikes = []; //All bikes other than your own
        private trailPoints = [];
        private registered = false;
        private version = 0.1;
        private gameStarted = false;

        private p5Instance : p5;

        constructor() {
            console.log("Started! 3");

            this.socket.on('client-registered', (pid) => {
                console.log("Registered as bike " + pid);
                this.socket.emit('request-world-update');
            });

            this.socket.on('world-update', ( data : WorldUpdateDto ) => {
                console.log("Got world update");
                console.log(data);
                if ( !this.gameStarted ) {
                    this.arena = new Arena( data.arena.dimensions );
                    this.p5Instance = new p5(this.sketch(), 'whatever');
                }
            });

            this.socket.emit('register');
        }

        private ui = {
            test : '#test'
        };

        private sketch() {
            return ( p : p5 ) => {
                p.setup = () => this.setup(p);
            }
        }

        private setup( p : p5 ) {
            let spacing = 10;

            p.createCanvas(this.arena.dimensions.x, this.arena.dimensions.y);

            this.arena.draw(p);

            //bg = new Background();
            //setupSocketListeners();
            console.log("Sent registration packet to " + this.host);
        }

        /*function setup() {
            player = new Player();
            bg = new Background();
            setupSocketListeners();
            socket.emit('register');
        }

            function draw() {
            bg.draw();
            if ( player.registered ) {
                textSize(32);
                fill(255);
                text(player.pid, 10, 30);

                player.update( trailPoints, gameWidth, gameHeight );
                player.draw();
            }
            _.each(bikes, function(b) {
                if (b) {
                    b.update(trailPoints, gameWidth, gameHeight);
                    b.draw();
                }
            });
        }

            function setupSocketListeners() {
            socket.on('register', function(data) {
                var gameState = data.gameState;
                var regData = data.regData;
                gameWidth = gameState.gameWidth;
                gameHeight = gameState.gameHeight;
                createCanvas(gameWidth, gameHeight);
                player.register( regData.pid, regData.x, regData.y, regData.xspeed, regData.yspeed );
                _.each( gameState.bikes, function(b) {
                    if (b && b.pid !== regData.pid ) {
                        addBike(b);
                    }
                });
            });

            /!*{
             trails: [{
             pid: number,
             points: [{
             x: number,
             y: number
             }]
             }],
             bikes: [{
             pid: number,
             x: number,
             y: number,
             xspeed: number,
             yspeed: number,
             isDead: boolean
             }]
             }*!/
            /!*socket.on('update', function(data) {
             console.log("got game update");
             console.log(JSON.stringify(data));
             var thisPid = player.pid;
             _.each( data.bikes, function(newBike) {
             if ( newBike && newBike.pid !== thisPid ) {
             var existingBike = bikes[newBike.pid];
             if ( existingBike ) {
             console.log(newBike.isDead);
             if ( !existingBike.isDead && newBike.isDead ) {
             // RIP
             existingBike.deathTimestamp = Math.floor(Date.now());
             }
             existingBike.x = newBike.x;
             existingBike.y = newBike.y;
             existingBike.xspeed = newBike.xspeed;
             existingBike.yspeed = newBike.yspeed;
             existingBike.isDead = newBike.isDead;
             console.log(existingBike.x);
             console.log(newBike.x);
             console.log(bikes[newBike.pid].x);
             console.log(existingBike.y);
             console.log(newBike.y);
             console.log(bikes[newBike.pid].y);
             console.log(newBike);
             //existingBike['trail'] = newBike.trail;
             } else {
             console.log("New bike");
             addBike(newBike);
             }
             }
             });
             });*!/

            socket.on('update', function(data) {
                var bikeData = data.bikeData;

                var existingBike = bikes[bikeData.pid];
                if ( existingBike ) {
                    if ( !existingBike.isDead && bikeData.isDead ) {
                        // RIP
                        existingBike.deathTimestamp = Math.floor(Date.now());
                    }
                    existingBike.x = bikeData.x;
                    existingBike.y = bikeData.y;
                    existingBike.xspeed = bikeData.xspeed;
                    existingBike.yspeed = bikeData.yspeed;
                    existingBike.isDead = bikeData.isDead;
                    existingBike.trail.points = bikeData.trail.points;

                    if ( bikeData.colour ) {
                        existingBike.colour = bikeData.colour;
                    }
                    if ( bikeData.trail ) {
                        existingBike.trail.colour = bikeData.trail.colour;
                        existingBike.trail.points = bikeData.trail.points;
                    }
                } else {
                    console.log("Requested bike doesn't exist!");
                    addBike(bikeData);
                }
            });

            socket.on('new_bike', function(newBike) {
                if ( newBike.pid !== this.pid ) {
                    addBike(newBike);
                }
            });

            socket.on('bike_left', function(pid) {
                delete bikes[pid];
                bikes.splice(pid, 1);
            });


        }

            function addBike(newBike) {
            var b = new Bike(newBike.pid, newBike.x, newBike.y, newBike.xspeed, newBike.yspeed);

            b.deathTimestamp = newBike.deathTimestamp;
            b.isDead = newBike.isDead;

            if ( newBike.colour ) {
                b.colour = newBike.colour;
            }
            if ( b.trail ) {
                b.trail.colour = newBike.trail.colour;
                b.trail.points = newBike.trail.points;
            }

            if ( b.isDead && !b.deathTimestamp ) {
                b.deathTimestamp =  Math.floor(Date.now());
            }

            bikes[newBike.pid] = b;
        }

            function keyPressed() {
            if (keyCode === UP_ARROW) {
                player.setDirection(0, -1);
            } else if (keyCode === DOWN_ARROW) {
                player.setDirection(0, 1);
            } else if (keyCode === RIGHT_ARROW) {
                player.setDirection(1, 0);
            } else if (keyCode === LEFT_ARROW) {
                player.setDirection(-1, 0);
            } else if (keyCode === 82) {
                //socket.emit('reset');
            }
        }*/

    }
    const game = new Game();


}