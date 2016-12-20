const express = require('express');
const app = express();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const _ = require('underscore');

var pidGen = 0;


var bikes = [];
var sockets = [];
const version = 0.1;



http.listen(3000, function() {
    console.log('listening on *:3000');
});

app.use(express.static(__dirname + '/client'));
app.get('/', function(req, res) {
    res.sendFile(__dirname + '/client/');
});

setInterval( function() {
    _.each(bikes, function(b) {
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

io.on('connection', function(socket) {
    console.log("CONNECTION!");
    socket.on('register', function() {
        var bikeData = generateBikeData();
        var pid = bikeData.pid;
        bikes[pid] = bikeData;
        sockets[pid] = socket;
        socket.emit('register', {
            gameState: {
                gameHeight: 500,
                gameWidth: 500,
                bikes: bikes
            },
            regData: bikeData
        });

        socket.broadcast.emit('update', { bikeData: bikeData });
        //socket.broadcast.emit('new_bike', bikes[pid]);
    });

    socket.on('update', function(data) {
        if (!data.v || data.v !== version) {
            socket.disconnect();
            return;
        }

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
            existingBike.trail = bikeData.trail;
/*
            console.log("EXISTING BIKE POST: xspeed=" + existingBike.xspeed + ", yspeed=" + existingBike.yspeed);
            console.log(existingBike === bikes[bikeData.pid]);*/
        } else {
            bikes[bikeData.pid] = data.bikeData;
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

    socket.on('reset', function() {
        console.log("reset event");
        var pid = null;
        _.any(sockets, function(s) {
            if ( s === socket ) {
                pid = sockets.indexOf(s);
                return true;
            }
            return false;
        });

        if ( pid !== null ) {
            bikes[pid] = generateBikeData(pid);
            socket.emit('bike_update', bikes[pid]);
            emitGameState(socket);
        }
    });

    var timeoutInMs = 10000;
    var timeout;
    socket.on('disconnect', function() {
        removePlayer(socket);

        var size = 0;
        for (i in bikes) {
            size++;
        }

        io.emit('bike_left', sockets.indexOf(socket));
        console.log('CHECKING SOCKETS', size + " sockets registered");
    });

    timeout = setTimeout(function() {
        removePlayer(socket);
    }, timeoutInMs);

});


function generateBikeData(existingPid) {
    var pid;
    if ( existingPid || existingPid === 0 ) {
        pid = existingPid;
    } else {
        pid = pidGen++;
    }
    var x = randInt(20, 480);
    var y = randInt(20, 480);
    var direction = randInt(1,4);
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
function emitGameState(socket) {
    var gameState = {
        bikes: bikes
    };
    io.emit('update', gameState);
}

function randInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randPlusMinus() {
    return Math.random() < 0.5 ? -1 : 1;
}

function removePlayer(socket) {
    for (pid in sockets) {
        if (sockets[pid] == socket) {
            console.log('PLAYER LEFT', pid);
            //io.emit('player left', guid);

            delete bikes[pid];
            bikes.splice(pid, 1);

            delete sockets[pid];
            sockets.splice(pid, 1);
        }
    }
};