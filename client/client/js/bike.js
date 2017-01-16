var scl = 0.2;

function Bike(pid, x, y, xspeed, yspeed) {
	this.pid = pid;
    this.x = x;
    this.y = y;
    this.xspeed = xspeed;
    this.yspeed = yspeed;
	this.explosionTime = 500; // .5 second
	this.explosionEnded = false;
    this.isDead = false;
    this.logging = false;

    var rgb = randomRgb();
    this.colour = rgba(rgb.r, rgb.g, rgb.b, 0.9);
    this.trailColour = rgba(rgb.r, rgb.g, rgb.b, 0.7);

    this.trail = new Trail(this.pid, this, this.trailColour);
    this.trail.addPoint(this.x, this.y);

    this.setDirection = function(x, y) {
        if ( this.xspeed !== 0 && x !== 0 ) { return false; }
        if ( this.yspeed !== 0 && y !== 0 ) { return false; }
        this.xspeed = x;
        this.yspeed = y;
        this.trail.addPoint(this.x, this.y);
        return true;
    };

    this.update = function() {
        if ( !this.isDead ) {
            this.x += this.xspeed * scl;
            this.y += this.yspeed * scl;
        }
    };

    this.draw = function() {
        if ( this.trail ) {
            this.trail.draw();
        }

        noStroke();
        if ( !this.isDead ) {
            fill( this.colour );
            ellipse(this.x, this.y, 5, 5);
        } else if ( !this.explosionEnded ) {
			if ( Math.floor(Date.now()) - this.deathTimestamp > this.explosionTime ) {
			    this.explosionEnded = true;
                return;
			}

	        // Explosion
	        fill('rgba(' + rand255() +', 0, 0, 0.50)');
	        ellipse(this.x, this.y, 20, 20);

	        var randCol = rand255();
	        fill('rgba(' + randCol +', ' + randCol + ', 0, 0.50)');
	        var randSize = Math.floor(Math.random() * 40);
	        ellipse(this.x, this.y, randSize, randSize);
        }
    };

    //TODO: pass in some kinda "collision object" or something instead of this mess..
    this.calculateCollision = function(trailPoints, gameWidth, gameHeight) {
        var collision = false;
		var collisionObj = null;

        for ( var i = 0; i < trailPoints.length - 1; i++ ) {
            var point = trailPoints[i];
	        var nextPoint = trailPoints[i+1];
			if ( nextPoint.id !== point.id ) { continue; }
			if ( !nextPoint.leading || point.id !== this.pid ) {
		        collision = collidePointLine(this.x, this.y, point.x, point.y, nextPoint.x, nextPoint.y);
		        if ( collision ) {
					collisionObj = {pid:this.pid, 'pid':point.id, 'npid':nextPoint.id};
					break;
				}
			}
        }

        collision = false; // remove this obviously

        collision = collision ||
                    this.x < 1 ||
                    this.x > gameWidth ||
                    this.y < 1 ||
                    this.y > gameHeight;

        if (collision) {
			this.log(collisionObj);
            this.die();
        }
    };

    this.die = function() {
        this.xspeed = 0;
        this.yspeed = 0;
        this.isDead = true;
		this.deathTimestamp = Math.floor(Date.now());
    };

    this.log = function(msg) {
        if (this.logging) {
            console.log(msg);
        }
    };
}
