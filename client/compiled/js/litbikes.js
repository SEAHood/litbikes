
/// <reference path="../typings/globals/socket.io-client/index.d.ts" />
/// <reference path="../typings/globals/underscore/index.d.ts" />
/// <reference path="../typings/p5.d.ts" />
/// <reference path="util.ts" />
/// <reference path="dto.ts" />
/// <reference path="model/arena.ts" />
/// <reference path="model/bike.ts" />
/// <reference path="game/game.ts" /> 

var LitBikes;
(function (LitBikes) {
    var Util;
    (function (Util_1) {
        class Vector {
            constructor(x, y) {
                this.x = x;
                this.y = y;
            }
        }
        Util_1.Vector = Vector;
        class Util {
            static randInt(min, max) {
                return Math.floor(Math.random() * (max - min + 1)) + min;
            }
        }
        Util_1.Util = Util;
        class Connection {
            constructor(socket, pid) {
                this.socket = socket;
                this.pid = pid;
            }
            fireWorldUpdated(worldUpdate) {
                this.socket.emit('world-update', worldUpdate);
            }
        }
        Util_1.Connection = Connection;
    })(Util = LitBikes.Util || (LitBikes.Util = {}));
})(LitBikes || (LitBikes = {}));

var LitBikes;
(function (LitBikes) {
    var Arena = Model.Arena;
    class Game {
        constructor() {
            this.host = 'http://localhost:9092';
            this.socket = io.connect(this.host);
            this.bikes = []; //All bikes other than your own
            this.trailPoints = [];
            this.registered = false;
            this.version = 0.1;
            this.gameStarted = false;
            this.ui = {
                test: '#test'
            };
            console.log("Started! 3");
            this.socket.on('client-registered', (pid) => {
                console.log("Registered as bike " + pid);
                this.socket.emit('request-world-update');
            });
            this.socket.on('world-update', (data) => {
                console.log("Got world update");
                console.log(data);
                if (!this.gameStarted) {
                    this.arena = new Arena(data.arena.dimensions);
                    this.p5Instance = new p5(this.sketch(), 'whatever');
                }
            });
            this.socket.emit('register');
        }
        sketch() {
            return (p) => {
                p.setup = () => this.setup(p);
            };
        }
        setup(p) {
            let spacing = 10;
            p.createCanvas(this.arena.dimensions.x, this.arena.dimensions.y);
            this.arena.draw(p);
            //bg = new Background();
            //setupSocketListeners();
            console.log("Sent registration packet to " + this.host);
        }
    }
    LitBikes.Game = Game;
    const game = new Game();
})(LitBikes || (LitBikes = {}));

var Model;
(function (Model) {
    class Arena {
        constructor(dim) {
            this.spacing = 10;
            this.dimensions = dim;
        }
        draw(p) {
            p.background(51);
            p.fill(255);
            p.textAlign('left', 'top');
            p.strokeWeight(1);
            p.stroke('rgba(125,249,255,0.10)');
            for (var i = 0; i < this.dimensions.x; i += this.spacing) {
                p.line(i, 0, i, this.dimensions.y);
            }
            for (var i = 0; i < this.dimensions.y; i += this.spacing) {
                p.line(0, i, this.dimensions.x, i);
            }
            p.text("LitBikes v0.1", 10, 10);
        }
    }
    Model.Arena = Arena;
})(Model || (Model = {}));

var Model;
(function (Model) {
    var Vector = LitBikes.Util.Vector;
    class Bike {
        constructor(bikeDto) {
            this.spdMagnitude = 0.2;
            this.isDead = false;
            this.deathTimestamp = null;
            this.pid = bikeDto.pid;
            this.pos = bikeDto.pos;
            this.spd = bikeDto.spd;
            this.isDead = bikeDto.isDead !== null ? false : bikeDto.isDead;
            this.deathTimestamp = bikeDto.deathTimestamp;
            this.trail = bikeDto.trail || [bikeDto.pos];
        }
        getPid() {
            return this.pid;
        }
        setSpeed(spd) {
            if ((!this.spd.x && !spd.x) || (!this.spd.y && !spd.y)) {
                return false;
            }
            this.spd = spd;
            this.trail.push(this.pos);
            return true;
        }
        update() {
            if (!this.isDead) {
                this.pos.x += this.spd.x * this.spdMagnitude;
            }
        }
        kill(timeOfDeath) {
            this.spd = new Vector(0, 0);
            this.isDead = true;
            this.deathTimestamp = timeOfDeath || Math.floor(Date.now());
        }
        draw(p) {
            p.noStroke();
            //p.fill('rgba(' + rand255() +', 0, 0, 0.50)');
            p.fill(230);
            p.ellipse(this.pos.x, this.pos.y, 20, 20);
            /*if ( !this.isDead ) {
                p.fill( this.colour );
                p.ellipse(this.x, this.y, 5, 5);
            } else if ( !this.explosionEnded ) {
                if ( Math.floor(Date.now()) - this.deathTimestamp > this.explosionTime ) {
                    this.explosionEnded = true;
                    return;
                }

                // Explosion
                p.fill('rgba(' + rand255() +', 0, 0, 0.50)');
                p.ellipse(this.x, this.y, 20, 20);

                var randCol = rand255();
                p.fill('rgba(' + randCol +', ' + randCol + ', 0, 0.50)');
                var randSize = Math.floor(Math.random() * 40);
                p.ellipse(this.x, this.y, randSize, randSize);
            }*/
        }
    }
    Model.Bike = Bike;
})(Model || (Model = {}));
