function Player() {
    this.pid = null;
    this.bike = null;
    this.registered = false;

    this.register = function( pid, x, y, xspeed, yspeed ) {
        this.pid = pid;
        this.bike = new Bike(pid, x, y, 1, 0);
        console.log(this.bike.colour);
        this.registered = true;
        console.log("REGISTERED WITH ID: " + this.pid);

        this.sendChangeEvent();
        var _this = this;
        setInterval(function(){
            _this.sendChangeEvent();
        }, 500);
    };

    this.setDirection = function(x, y) {
        if ( this.bike.setDirection(x, y) ) {
            this.sendChangeEvent();
        }
    };




    this.getTrailPoints = function() {
        //todo these should really be sorted explicitly just in case
        var points = this.trail.points.slice();
        points.push({'x': this.x, 'y': this.y, 'leading': true});
        return points;
    };
    this.sendChangeEvent = function() {
        var eventData = {
            v: version,
            bikeData: {
                pid: this.pid,
                x: this.bike.x,
                y: this.bike.y,
                xspeed: this.bike.xspeed,
                yspeed: this.bike.yspeed,
                isDead: this.bike.isDead,
                trail: this.bike.trail,
                colour: this.bike.colour
            }

        };
        console.log("sending");
        console.log(eventData);
        socket.emit('update', eventData)
    };

    this.update = function(trailPoints, gameWidth, gameHeight) {
        if ( this.bike ) {
            var wasDead = this.bike.isDead;
            this.bike.update();
            if ( wasDead !== this.bike.isDead ) {
                this.sendChangeEvent();
            } else if ( !this.bike.isDead ) {
                this.bike.calculateCollision(trailPoints, gameWidth, gameHeight);
            }
        }
    };

    this.draw = function() {
        if ( this.bike ) {
            this.bike.draw();
        }
    };
}