function Trail(pid, bike, colour) {
    this.points = new Array();
    this.colour = colour;

    this.clear = function() {
        this.points = new Array();
    };

    this.addPoint = function(x, y) {
        this.points.push({ "pid": pid, 'x': x, 'y': y });
    };

    this.draw = function() {
        strokeWeight(2);
        stroke( this.colour );

        for ( var i = 0; i < this.points.length; i++ ) {
            var point = this.points[i];
            var nextPoint;

            if ( i < this.points.length - 1 ) {
                nextPoint = this.points[i+1];
            } else {
                nextPoint = { 'pid': pid, 'x': bike.x, 'y': bike.y };
            }
            line(point.x, point.y, nextPoint.x, nextPoint.y);
        }
    };
}
