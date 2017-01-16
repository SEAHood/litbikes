function Background() {
    this.spacing = 50;

    this.draw = function() {
        var height = 500;
        var width = 500;

        background(51);
        strokeWeight(1);
        stroke('rgba(125,249,255,0.10)');

        for ( var i = 0; i < width; i += this.spacing ) {
            line(i, 0, i, height);
        }

        for ( var i = 0; i < height; i += this.spacing ) {
            line(0, i, width, i);
        }
    }
}