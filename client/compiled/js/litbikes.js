
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
        var Vector = (function () {
            function Vector(x, y) {
                this.x = x;
                this.y = y;
            }
            return Vector;
        }());
        Util_1.Vector = Vector;
        var Util = (function () {
            function Util() {
            }
            Util.randInt = function (min, max) {
                return Math.floor(Math.random() * (max - min + 1)) + min;
            };
            return Util;
        }());
        Util_1.Util = Util;
        var Connection = (function () {
            function Connection(socket, pid) {
                this.socket = socket;
                this.pid = pid;
            }
            Connection.prototype.fireWorldUpdated = function (worldUpdate) {
                this.socket.emit('world-update', worldUpdate);
            };
            return Connection;
        }());
        Util_1.Connection = Connection;
    })(Util = LitBikes.Util || (LitBikes.Util = {}));
})(LitBikes || (LitBikes = {}));

var LitBikes;
(function (LitBikes) {
    var Arena = Model.Arena;
    var Game = (function () {
        function Game() {
            var _this = this;
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
            this.socket.on('client-registered', function (pid) {
                console.log("Registered as bike " + pid);
                _this.socket.emit('request-world-update');
            });
            this.socket.on('world-update', function (data) {
                console.log("Got world update");
                console.log(data);
                if (!_this.gameStarted) {
                    _this.arena = new Arena(data.arena.dimensions);
                    _this.p5Instance = new p5(_this.sketch(), 'whatever');
                }
            });
            this.socket.emit('register');
        }
        Game.prototype.sketch = function () {
            var _this = this;
            return function (p) {
                p.setup = function () { return _this.setup(p); };
            };
        };
        Game.prototype.setup = function (p) {
            var spacing = 10;
            p.createCanvas(this.arena.dimensions.x, this.arena.dimensions.y);
            this.arena.draw(p);
            //bg = new Background();
            //setupSocketListeners();
            console.log("Sent registration packet to " + this.host);
        };
        return Game;
    }());
    LitBikes.Game = Game;
    var game = new Game();
})(LitBikes || (LitBikes = {}));

var Model;
(function (Model) {
    var Arena = (function () {
        function Arena(dim) {
            this.spacing = 10;
            this.dimensions = dim;
        }
        Arena.prototype.draw = function (p) {
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
        };
        return Arena;
    }());
    Model.Arena = Arena;
})(Model || (Model = {}));

var Model;
(function (Model) {
    var Vector = LitBikes.Util.Vector;
    var Bike = (function () {
        function Bike(bikeDto) {
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
        Bike.prototype.getPid = function () {
            return this.pid;
        };
        Bike.prototype.setSpeed = function (spd) {
            if ((!this.spd.x && !spd.x) || (!this.spd.y && !spd.y)) {
                return false;
            }
            this.spd = spd;
            this.trail.push(this.pos);
            return true;
        };
        Bike.prototype.update = function () {
            if (!this.isDead) {
                this.pos.x += this.spd.x * this.spdMagnitude;
            }
        };
        Bike.prototype.kill = function (timeOfDeath) {
            this.spd = new Vector(0, 0);
            this.isDead = true;
            this.deathTimestamp = timeOfDeath || Math.floor(Date.now());
        };
        Bike.prototype.draw = function (p) {
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
        };
        return Bike;
    }());
    Model.Bike = Bike;
})(Model || (Model = {}));
