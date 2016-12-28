module LitBikes {
    const _ = require('underscore');

    export class Server {
        private express = require('express');
        private app = this.express();
        private http = require('http').Server(this.app);
        private io = require('socket.io')(this.http);

        private pidGen = 0;


        private bikes : any[] = [];
        private sockets = [];
        private version = 0.1;

        constructor() {
            this.http.listen(1337, () => {
                console.log('LITBIKES - READY TO ROCK AND ROLL ON PORT 1337');
            });

            this.app.use(this.express.static('client/'));
            this.app.get('/', (req, res) => {
                res.sendFile('client/');
            });

            setInterval( () => {
                _.each(this.bikes, b => {
                    if (b) {
                        console.log("BIKE-------------------");
                        console.log(b.pid);
                        console.log(b.x);
                        console.log(b.y);
                        console.log(b.isDead);
                        console.log("END BIKE---------------");
                    }
                });
            }, 1000);

            this.io.on('connection', socket => {
                console.log("CONNECTION!");
                socket.on('register', () => {
                    var bikeData = this.generateBikeData(null);
                    var pid = bikeData.pid;
                    this.bikes[pid] = bikeData;
                    this.sockets[pid] = socket;
                    socket.emit('register', {
                        gameState: {
                            gameHeight: 500,
                            gameWidth: 500,
                            bikes: this.bikes
                        },
                        regData: bikeData
                    });

                    socket.broadcast.emit('update', { bikeData: bikeData });
                    //socket.broadcast.emit('new_bike', bikes[pid]);
                });

                socket.on('update', data => {
                    if (!data.v || data.v !== this.version) {
                        socket.disconnect();
                        return;
                    }

                    var bikeData = data.bikeData;
                    var existingBike = this.bikes[bikeData.pid];
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
                        existingBike.trail = bikeData.trail;
                        /*
                         console.log("EXISTING BIKE POST: xspeed=" + existingBike.xspeed + ", yspeed=" + existingBike.yspeed);
                         console.log(existingBike === bikes[bikeData.pid]);*/
                    } else {
                        this.bikes[bikeData.pid] = data.bikeData;
                        /*bikes[bikeData.pid] = {
                         pid: bikeData.pid,
                         x: bikeData.x,
                         y: bikeData.y,
                         xspeed: bikeData.xspeed,
                         yspeed: bikeData.yspeed,
                         isDead: bikeData.isDead
                         };
                         sockets[bikeData.pid] = socket;*/
                    }

                    socket.broadcast.emit('update', data);

                    //emitGameState(socket);
                });

                socket.on('reset', () => {
                    console.log("reset event");
                    var pid = null;
                    _.any(this.sockets, s => {
                        if ( s === socket ) {
                            pid = this.sockets.indexOf(s);
                            return true;
                        }
                        return false;
                    });

                    if ( pid !== null ) {
                        this.bikes[pid] = this.generateBikeData(pid);
                        socket.emit('bike_update', this.bikes[pid]);
                        this.emitGameState(socket);
                    }
                });

                var timeoutInMs = 10000;
                var timeout;
                socket.on('disconnect', () => {
                    this.removePlayer(socket);



                    this.io.emit('bike_left', this.sockets.indexOf(socket));
                    console.log('CHECKING SOCKETS', this.sockets.length + " sockets registered");
                });

                timeout = setTimeout(() => {
                    this.removePlayer(socket);
                }, timeoutInMs);

            });
        }

        generateBikeData(existingPid) {
            var pid;
            if ( existingPid || existingPid === 0 ) {
                pid = existingPid;
            } else {
                pid = this.pidGen++;
            }
            var x = this.randInt(20, 480);
            var y = this.randInt(20, 480);
            var direction = this.randInt(1,4);
            var xspeed, yspeed;
            switch (direction) {
                case 1:
                    xspeed = 0;
                    yspeed = -1;
                    break;
                case 2:
                    xspeed = 0;
                    yspeed = 1;
                    break;
                case 3:
                    xspeed = -1;
                    yspeed = 0;
                    break;
                case 4:
                    xspeed = 1;
                    yspeed = 0;
                    break;
                default:
                    break;
            }

            return {
                pid: pid,
                x: x,
                y: y,
                xspeed: xspeed,
                yspeed: yspeed
            }
        }

        emitGameState(socket) {
            var gameState = {
                bikes: this.bikes
            };
            this.io.emit('update', gameState);
        }

        randInt(min, max) {
            return Math.floor(Math.random() * (max - min + 1)) + min;
        }

        randPlusMinus() {
            return Math.random() < 0.5 ? -1 : 1;
        }

        removePlayer(socket) {
            _.each( this.sockets, s => {
                if ( s == socket ) {
                    let pid = this.sockets.indexOf(s);
                    console.log('PLAYER LEFT', pid);
                    //io.emit('player left', guid);

                    delete this.bikes[pid];
                    this.bikes.slice(pid, 1);

                    delete this.sockets[pid];
                    this.sockets.slice(pid, 1);
                }
            });
        };
    }
    const server = new Server();
}