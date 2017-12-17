module Model {

    import Vector = Util.Vector;
    import ArenaDto = Dto.ArenaDto;
    import NumberUtil = Util.NumberUtil;
    export class Arena {
        public size : number;
        private spacing = 10;

        constructor( dto : ArenaDto ) {
            this.size = dto.size;
        }

        public draw( p : p5 ) {
            p.background(51);            
            // THE OLD BLUEISH GRID
            // p.strokeWeight(1);
            // p.stroke('rgba(125,249,255,0.10)');

            // for (var i = 0; i < this.size; i += this.spacing ) {
            //     p.line(i, 0, i, this.size);
            // }

            // for (var i = 0; i < this.size; i += this.spacing ) {
            //     p.line(0, i, this.size, i);
            // }            
        }
    }
}