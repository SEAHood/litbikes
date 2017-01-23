module Model {

    import Vector = Util.Vector;
    import ArenaDto = Dto.ArenaDto;
    export class Arena {

        public dimensions : Vector;
        private spacing = 10;

        constructor( dto : ArenaDto ) {
            this.dimensions = dto.dimensions;
        }

        public draw( p : p5 ) {

            p.background(51);
            p.strokeWeight(1);
            p.stroke('rgba(125,249,255,0.10)');

            for (var i = 0; i < this.dimensions.x; i += this.spacing ) {
                p.line(i, 0, i, this.dimensions.y);
            }

            for (var i = 0; i < this.dimensions.y; i += this.spacing ) {
                p.line(0, i, this.dimensions.x, i);
            }

        }

    }
}