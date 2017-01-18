function AI() {
    this.bike = new Bike('ai', Math.floor(Math.random() * windowWidth), Math.floor(Math.random() * windowHeight));
    this.bike.logging = true;

	this.reset = function() {
    	this.bike = new Bike('ai', Math.floor(Math.random() * windowWidth), Math.floor(Math.random() * windowHeight));
	};

    this.update = function(trailPoints) {
        this.bike.update(trailPoints);
		if ( !this.bike.isDead ) {
			this.predictCollision(trailPoints);
		} else if ( this.bike.explosionEnded ) {
			this.reset();
		}
    };

    this.draw = function() {
        this.bike.draw();
    };

	this.predictCollision = function(trailPoints) {
		var collision = false;
		var collisionObj = null;
		var dDist = 20;

        for ( var i = 0; i < trailPoints.length - 1; i++ ) {
            var point = trailPoints[i];
	        var nextPoint = trailPoints[i+1];


	        collision = collidePointLine(this.bike.x + (dDist*this.bike.xspeed), this.bike.y + (dDist*this.bike.yspeed), point.x, point.y, nextPoint.x, nextPoint.y);
	        if ( collision ) {
				collisionObj = {id:this.id, 'pid':point.id, 'npid':nextPoint.id};
				break;
			}
        }

		if ( this.bike.xspeed !== 0 ) {
			if ( this.bike.xspeed < 0 ) {
				collision = collision || this.bike.x - dDist < 1;
			} else {
				collision = collision || this.bike.x + dDist > maxWidth;
			}
		} else if ( this.bike.yspeed !== 0 ) {
			if ( this.bike.yspeed < 0 ) {
				collision = collision || this.bike.y- dDist < 1;
			} else {
				collision = collision || this.bike.y + dDist > maxHeight;
			}
		}

        if (collision) {
			var newVal = Math.random() < 0.5 ? -1 : 1;
			if ( this.bike.xspeed !== 0 ) {
				this.bike.direction( 0, newVal );
			} else if ( this.bike.yspeed !== 0 ) {
				this.bike.direction( newVal, 0 );
			}
			console.log("ABOUT TO COLLIDE!");
        }
	};

	this.getTrailPoints = function() {
		return this.bike.getTrailPoints();
	};
}
