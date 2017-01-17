
/// <reference path="../typings/globals/socket.io-client/index.d.ts" />
/// <reference path="../typings/globals/underscore/index.d.ts" />
/// <reference path="../typings/p5.d.ts" />
/// <reference path="util.ts" />
/// <reference path="dto.ts" />
/// <reference path="model/arena.ts" />
/// <reference path="model/bike.ts" />
/// <reference path="game/game.ts" /> 

var Util;
(function (Util_1) {
    var Vector = (function () {
        function Vector(x, y) {
            this.x = x;
            this.y = y;
        }
        Vector.prototype.add = function (x, y) {
            this.x += x;
            this.y += y;
        };
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
})(Util || (Util = {}));

var Model;
(function (Model) {
    var Arena = (function () {
        function Arena(dim) {
            this.spacing = 10;
            this.dimensions = dim;
        }
        Arena.prototype.draw = function (p) {
            p.background(51);
            p.strokeWeight(1);
            p.stroke('rgba(125,249,255,0.10)');
            for (var i = 0; i < this.dimensions.x; i += this.spacing) {
                p.line(i, 0, i, this.dimensions.y);
            }
            for (var i = 0; i < this.dimensions.y; i += this.spacing) {
                p.line(0, i, this.dimensions.x, i);
            }
        };
        return Arena;
    }());
    Model.Arena = Arena;
})(Model || (Model = {}));

var Model;
(function (Model) {
    var Vector = Util.Vector;
    var Bike = (function () {
        function Bike(bikeDto) {
            this.spdMagnitude = 0.4;
            this.isDead = false;
            this.deathTimestamp = null;
            this.pid = bikeDto.pid;
            this.pos = bikeDto.pos;
            this.spd = bikeDto.spd;
            this.isDead = bikeDto.isDead !== null ? false : bikeDto.isDead;
            this.deathTimestamp = bikeDto.deathTimestamp;
            this.trail = bikeDto.trail || [bikeDto.pos];
        }
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
                var xDiff = this.spd.x * this.spdMagnitude;
                var yDiff = this.spd.y * this.spdMagnitude;
                this.pos.add(xDiff, yDiff);
            }
        };
        Bike.prototype.updateFromDto = function (dto) {
            this.pos = new Vector(dto.pos.x, dto.pos.y);
            this.spd = new Vector(dto.spd.x, dto.spd.y);
            this.isDead = dto.isDead;
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
            p.ellipse(this.pos.x, this.pos.y, 5, 5);
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
        Bike.prototype.getPid = function () {
            return this.pid;
        };
        Bike.prototype.getPos = function () {
            return this.pos;
        };
        Bike.prototype.getSpd = function () {
            return this.spd;
        };
        return Bike;
    }());
    Model.Bike = Bike;
})(Model || (Model = {}));

var Game;
(function (Game_1) {
    var Bike = Model.Bike;
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
            this.socket.on('client-registered', function (data) {
                console.log("Registered as bike " + data.pid);
                _this.player = new Bike(data);
                _this.socket.emit('request-world-update');
            });
            setInterval(function () { return _this.socket.emit('request-world-update'); }, 1000);
            this.socket.on('world-update', function (data) {
                console.log("Got world update");
                console.log(data);
                _.each(data.bikes, function (b) {
                    if (b.pid === _this.player.getPid()) {
                        _this.player.updateFromDto(b);
                    }
                });
                if (!_this.gameStarted) {
                    _this.arena = new Arena(data.arena.dimensions);
                    _this.p5Instance = new p5(_this.sketch(), '#game');
                    _this.gameStarted = true;
                }
            });
            this.socket.emit('register');
            console.log("Sent registration packet to " + this.host);
        }
        Game.prototype.sketch = function () {
            var _this = this;
            return function (p) {
                p.setup = function () { return _this.setup(p); };
                p.draw = function () { return _this.draw(p); };
            };
        };
        Game.prototype.draw = function (p) {
            console.log("Drawing! " + new Date());
            this.player.update();
            this.arena.draw(p);
            p.fill(255);
            p.textAlign('left', 'top');
            p.text("LitBikes 0.0", 10, 10);
            p.text("pid: " + this.player.getPid() + "\n" +
                "pos: " + this.player.getPos().x.toFixed(0) + ", " + this.player.getPos().y.toFixed(0) + "\n" +
                "spd: " + this.player.getSpd().x + ", " + this.player.getSpd().y, 10, 30, 300, 500);
            this.player.draw(p);
        };
        Game.prototype.setup = function (p) {
            p.createCanvas(this.arena.dimensions.x, this.arena.dimensions.y);
            this.arena.draw(p);
            //bg = new Background();
            //setupSocketListeners();
        };
        return Game;
    }());
    Game_1.Game = Game;
    new Game();
})(Game || (Game = {}));
