/// <reference path="../../typings/globals/socket.io/index.d.ts" />
/// <reference path="../../typings/globals/underscore/index.d.ts" />

var ThreadedLock = require("threaded-lock");
import {BikeDto} from "../dto";
import {Connection} from "../util";
import {GameWorld} from "./world";

//module Server {

    export class Server {
        private express = require('express');
        private app = this.express();
        private http = require('http').Server(this.app);
        private io = require('socket.io')(this.http);

        private gameWorld = new GameWorld();
        private connections : Connection[] = [];
        private version = 0.1;

        constructor() {
            this.http.listen(1337, () => {
                console.log('LITBIKES - READY TO ROCK AND ROLL ON PORT 1337');
            });

            this.app.use(this.express.static('client/'));
            this.app.get('/', (req, res) => {
                res.sendFile('compiled/client/');
            });

            this.app.get('/server-view', (req, res) => {
                res.writeHead(200, {'Content-Type': 'text/plain'});
                res.end('Hello World\n');
            });

            this.io.on('connection', socket => {
                socket.on('register', () => {
                    console.log("REGISTER!");
                    /*let pid = this.gameWorld.newBike();
                    this.connections.push( new Connection( socket, pid ) );
                    this.worldUpdated();*/
                });

                socket.on('bike-update', ( data : BikeDto ) => {
                    this.gameWorld.handleUpdate( data );
                    this.worldUpdated();
                });

                socket.on('disconnect', () => {
                    this.killSocket(socket);
                    this.worldUpdated();
                });


                // TODO RESET

                // TODO TIMEOUT

            });
        }

        private async killSocket( socket : any ) {
            let pid = null;
            let tlc = new ThreadedLock("connections-lock");
            await tlc.lock();
            this.connections = _.filter( this.connections, ( c : Connection ) => {
                if ( c.socket === socket ) {
                    pid = c.pid;
                    return false;
                } else {
                    return true;
                }
            });
            tlc.unlock();

            if ( pid !== null ) {
                this.gameWorld.removeBike(pid);
            }
        }

        private worldUpdated() {
            _.each( this.connections, ( c : Connection ) => {
                c.fireWorldUpdated( this.gameWorld.getWorldDto( c.pid ) );
            });
        }

    }

    // kick off the server
    const server = new Server();
//}