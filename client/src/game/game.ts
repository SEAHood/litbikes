
module Game {

    import Bike = Model.Bike;
    import Arena = Model.Arena;
    import WorldUpdateDto = Dto.WorldUpdateDto;
    import BikeDto = Dto.BikeDto;
    import Vector = Util.Vector;
    import ClientUpdateDto = Dto.ClientUpdateDto;
    import GameJoinDto = Dto.GameJoinDto;
    import ClientGameJoinDto = Dto.ClientGameJoinDto;
    import HelloDto = Dto.HelloDto;
    import NumberUtil = Util.NumberUtil;
    import ChatMessageDto = Dto.ChatMessageDto;
    import ScoreDto = Dto.ScoreDto;
    export class Game {
        private player : Bike;
        private host = 'http://' + window.location.hostname + ':9092';
        private socket = io.connect(this.host);
        private arena : Arena;
        private bikes : Bike[] = [];
        private registered = false;
        private version ="0.1b";
        private gameStarted = false;
        private gameJoined = false;
        private gameTickMs : number;
        private p5Instance : p5;
        private showDebug = true;
        private showRespawn = true;
        private timeKeepAliveSent : number;
        private latency : number;
        private gameTick : number;
        private roundInProgress: boolean;
        private roundTimeLeft: number;
        private messageCount : number;
        private tabPressed : boolean;
        private serverTimeoutTimer : number;
        private serverTimedOut : boolean = false;

        private nameInputField : any;
        private nameInputButton : any;

        private mainFont;
        private secondaryFont;
        private debugFont;

        constructor() {
            this.socket.on('hello', ( data : HelloDto ) => {
                this.initGame(data);
            });

            this.socket.on('joined-game', (data : GameJoinDto ) => {
                this.joinGame(data);
            });

            setInterval(() => {
                this.timeKeepAliveSent = new Date().getTime();
                this.socket.emit('keep-alive');
            }, 1000);

            this.socket.on('keep-alive-ack', data => {
                let timeNow = new Date().getTime();
                this.latency = timeNow - this.timeKeepAliveSent;
                this.refreshServerTimeout();
            });

            this.socket.on('world-update', ( data : WorldUpdateDto ) => {
                if ( this.gameStarted ) {
                    this.processWorldUpdate(data);
                }
            });

            this.socket.on('score-update', (data: ScoreDto[]) => {
                this.updateScores(data);
            });

            this.socket.on('chat-message', ( data : ChatMessageDto ) => {
                // TODO: Use moment or something?
                let messageTime = new Date(data.timestamp).toTimeString().split(' ')[0];
                let chatElement = "<li>";

                chatElement += "[" + messageTime + "]";
                if ( data.isSystemMessage ) {
                    chatElement += "&nbsp;<span style='color:#AFEEEE'>" + _.escape(data.message) + "</span>";
                } else {
                    let colour = data.sourceColour.replace("%A%", "100");
                    chatElement += "&nbsp;<span style='color:" + colour + "'><strong>" + data.source + "</strong></span>:";
                    chatElement += "&nbsp;" + _.escape(data.message);
                }
                chatElement += "</li>";

                $('#chat-log ul').append(chatElement);
                $('#chat-log').scrollTop($('#chat-log')[0].scrollHeight);

                if ( $('#message-list li').length > 250 ) {
                    $('#message-list li').first().remove();
                }

                this.messageCount = $("#message-list li").length;
            });

            enum Keys {
                LEFT_ARROW = 37,
                UP_ARROW = 38,
                RIGHT_ARROW = 39,
                DOWN_ARROW = 40,
                W = 87,
                A = 65,
                S = 83,
                D = 68,
                R = 82,
                F3 = 114,
                H = 72,
                TAB = 9
            };

            $(document).on('keyup', ev => {
                let keyCode = ev.which;
                if ( this.player ) {
                    if (keyCode === Keys.TAB) {
                        this.tabPressed = false;
                    }
                }
            });

            $(document).on('keydown', ev => {
                if ( $(ev.target).is('input') ) {
                    // Typing in chat, don't process as game keys
                    if ( ev.which === 13 ) { // enter
                        if ($(ev.target).is('#player-name-input')) { // enter when inside player name box
                            let name = $('#player-name-input').val();
                            if (this.nameIsValid(name)) {
                                this.requestJoinGame(name);
                            }                            
                        } else if ($(ev.target).is('#chat-input')) { // enter when inside chat box
                            let message = $('#chat-input').val();
                            if (message.trim() != "") {
                                this.socket.emit('chat-message', message);
                                $('#chat-input').val('');
                            }
                        }
                    }
                    return;
                }
                
                if ( this.player ) {

                    let keyCode = ev.which;
                    let newVector = null;
                    let eventMatched = true;

                    if (keyCode === Keys.UP_ARROW || keyCode === Keys.W) {
                        newVector = new Vector(0, -1);
                    } else if (keyCode === Keys.DOWN_ARROW || keyCode === Keys.S) {
                        newVector = new Vector(0, 1);
                    } else if (keyCode === Keys.RIGHT_ARROW || keyCode === Keys.D) {
                        newVector = new Vector(1, 0);
                    } else if (keyCode === Keys.LEFT_ARROW || keyCode === Keys.A) {
                        newVector = new Vector(-1, 0);
                    } else if (keyCode === Keys.F3) {
                        this.showDebug = !this.showDebug;
                    } else if (keyCode === Keys.R) {
                        this.socket.emit('request-respawn');
                    } else if (keyCode === Keys.H) {
                        this.showRespawn = !this.showRespawn;
                    } else if (keyCode === Keys.TAB) {
                        this.tabPressed = true;
                    } else {
                        eventMatched = false;
                    }

                    if ( eventMatched ) {
                        ev.preventDefault();
                        ev.stopPropagation();
                    }

                    if ( newVector ) {
                        //this.player.setDirection(newVector);
                        //this.sendClientUpdate();
                        // TODO MOVE THIS SOMEWHERE ELSE
                        let updateDto : ClientUpdateDto = {
                            pid : this.player.getPid(),
                            xDir : newVector.x,
                            yDir : newVector.y,
                            xPos : this.player.getPos().x,
                            yPos : this.player.getPos().y
                        };
                        this.socket.emit('update', updateDto);
                    }
                }
            });
            
            $(document).ready(() => {                
                $('#player-name-input').on('input', () => {
                    let name = $('#player-name-input').val();
                    if (this.nameIsValid(name)) {
                        $('#player-name-submit').show();
                    } else {
                        $('#player-name-submit').hide();
                    }
                });
                
                $('#player-name-submit').on('click', () => {
                    let name = $('#player-name-input').val();
                    this.requestJoinGame(name);
                });
            });

            this.socket.emit('hello');
        }

        private nameIsValid(name: string): boolean {
            return name.trim().length > 1 && name.trim().length <= 15;
        }

        private refreshServerTimeout() {
            if (this.serverTimeoutTimer) {
                clearInterval(this.serverTimeoutTimer);
                this.serverTimeoutTimer = null;
            }
            this.serverTimedOut = false;
            this.serverTimeoutTimer = setInterval(() => this.serverTimedOut = true, 5000);
        }

        private requestJoinGame(name: string) {
            let joinObj : ClientGameJoinDto = {
                name: name
            };
            this.socket.emit('request-join-game', joinObj);
        }

        private joinGame( data : GameJoinDto ) {
            $('#welcome-container').hide();
            $('#info-container').slideDown();
            this.gameJoined = true;
            this.player = new Bike(data.bike, true);
            this.updateScores(data.scores);
        }

        private initGame( data : HelloDto ) {
            if ( !data.gameSettings.gameTickMs ) {
                console.error("Cannot start game - game tick interval is not defined");
            }
            this.gameTick = data.world.gameTick;
            this.arena = new Arena(data.world.arena);
            this.gameTickMs = data.gameSettings.gameTickMs;
            
            this.processWorldUpdate(data.world);

            this.p5Instance = new p5(this.sketch(), 'game-container');
            this.gameStarted = true;
            
            $('#game').width(this.arena.size);
            $('#game').height(this.arena.size);

            // MAIN UPDATE LOOP
            setInterval(() => {
                this.gameTick++;
                var baseSpeed = 1.5; // TODO: Get this from the server!
                if (this.gameJoined) {
                    // Faster farther from center - disabled just now
                    //let spdModifier = this.calculateSpeedModifier(this.player);
                    //this.player.setSpd(baseSpeed + spdModifier)
                    this.player.update();
                }
                _.each( this.bikes, ( b : Bike ) => {
                    // Faster farther from center - disabled just now      
                    //let spdModifier = this.calculateSpeedModifier(b);                         
                    //b.setSpd(baseSpeed + spdModifier);
                    b.update();
                });
            }, this.gameTickMs);
        }

        private calculateSpeedModifier( b : Bike ): number {            
            let gameSize = this.arena.size;
            let center = new Vector(gameSize/2, gameSize/2);
            let bikePos = new Vector(b.getPos().x, b.getPos().y),
                distance = Vector.distance(bikePos, center),
                oldMin = 0,
                oldMax = gameSize/2,
                newMin = 0,
                newMax = 0.5,
                oldRange = oldMax - oldMin,
                newRange = newMax - newMin,
                spdModifier = ((distance - oldMin) * newRange / oldRange) + newMin; // Trust me
            return spdModifier;
        }
        
        private processWorldUpdate( data : WorldUpdateDto ) {
            this.roundInProgress = data.roundInProgress;
            this.roundTimeLeft = data.roundTimeLeft;
            //console.log("Processing world update");
            let updatedBikes = _.pluck(data.bikes, 'pid');
            let existingBikes = _.pluck(this.bikes, 'pid');
            _.each( existingBikes, ( pid : number ) => {
                if ( !_.contains(updatedBikes, pid ) ) {
                    this.bikes = _.reject(this.bikes, ( b : Bike ) => b.getPid() === pid );
                }
            });

            _.each( data.bikes, ( b : BikeDto ) => {
                if ( this.gameJoined && b.pid === this.player.getPid() && this.player ) {
                    this.player.updateFromDto(b);
                } else {
                    let bike = _.find(this.bikes, (bike:Bike) => bike.getPid() === b.pid);
                    if ( bike ) {
                        bike.updateFromDto(b);
                    } else {
                        this.bikes.push(new Bike(b, false));
                    }
                }
            });
        }

        private updateScores(scores: ScoreDto[]) {            
            scores = _.sortBy(scores, x => x.score).reverse();
            let topFive = _.first(scores, 5);
            $('#score ul').empty();
            let playerInTopFive = false;
            topFive.forEach((score: ScoreDto, i: number) => {                
                let isPlayer = this.gameJoined && score.pid == this.player.getPid();
                let player : Bike = _.first(this.bikes.filter((b: Bike) => b.getPid() == score.pid));
                playerInTopFive = isPlayer || playerInTopFive;
                let li = isPlayer ? "<li style='color:yellow'>" : "<li>";
                let position = "#" + (i + 1);
                let bikeColour = !player || isPlayer 
                    ? "inherit"
                    :  player.getColour().replace('%A%', '1');
                let scoreElement = li + position + ": <span style='color:" + bikeColour + "'>" + score.name + "</span> - " + score.score + "</li>";
                $('#score ul').append(scoreElement);
            });

            if (this.gameJoined && !playerInTopFive) {
                let playerScore = scores.filter(x => x.pid == this.player.getPid())[0];
                if (!playerScore) {
                    return;
                }
                let li = "<li style='color:yellow'>";
                let position = "#" + (scores.indexOf(playerScore) + 1);
                let scoreElement = li + position + ": " + playerScore.name + " - " + playerScore.score + "</li>";
                $('#score ul').append(scoreElement);
            }
        }

        private sketch() {
            return ( p : p5 ) => {
                p.setup = () => this.setup(p);
                p.draw = () => this.draw(p);
            }
        }

        private draw( p : p5 ) {
            let halfWidth = this.arena.size / 2;
            let halfHeight = this.arena.size / 2;

            this.arena.draw(p);
            
            _.each( this.bikes, ( b : Bike ) => {
                b.draw(p, false, this.tabPressed);
            });

            if ( this.serverTimedOut ) {
                p.noStroke();
                p.fill('rgba(0,0,0,0.6)');
                p.rect(0, halfHeight - 35, this.arena.size, 55);

                p.textFont(this.mainFont);
                p.textAlign('center', 'top');

                p.fill('rgba(125,249,255,0.50)');
                p.textSize(29);
                p.text("He's dead, Jim",
                    halfWidth + NumberUtil.randInt(0, 2), halfHeight - 30 + NumberUtil.randInt(0, 2));
                p.fill('rgba(255,255,255,0.80)');
                p.textSize(28);
                p.text("He's dead, Jim",
                    halfWidth, halfHeight - 30);

                p.fill('rgba(0,0,0,0.40)');
                p.fill(255);
                p.textFont(this.secondaryFont);
                p.textSize(15);
                p.text("Lost connection to the server", halfWidth, halfHeight);

                return;
            }

            if (this.gameJoined) {
                this.player.draw(p, this.player.isRespawning(), this.tabPressed);

                if ( this.player.isCrashed() && this.player.isSpectating() && this.showRespawn ) {
                    p.noStroke();
                    p.fill('rgba(0,0,0,0.6)');
                    p.rect(0, halfHeight - 35, this.arena.size, 100);

                    p.textFont(this.mainFont);
                    p.textAlign('center', 'top');

                    if ( this.player.isCrashed() ) {
                        let suicide = this.player.getCrashedInto() == this.player.getPid();
                        let crashText = "";
                        if (suicide) {
                            crashText = "You killed yourself, idiot";
                        } else {
                            crashText = "Killed by " + this.player.getCrashedIntoName();
                        }
                        p.fill('rgba(125,249,255,0.50)');
                        p.textSize(29);
                        p.text(crashText,
                            halfWidth + NumberUtil.randInt(0, 2), halfHeight - 30 + NumberUtil.randInt(0, 2));
                        p.fill('rgba(255,255,255,0.80)');
                        p.textSize(28);
                        p.text(crashText,
                            halfWidth, halfHeight - 30);
                    }

                    p.fill('rgba(125,249,255,0.50)');
                    p.textSize(33);
                    p.text("Press 'R' to respawn",
                        halfWidth + NumberUtil.randInt(0, 2), halfHeight + NumberUtil.randInt(0, 2));

                    p.fill('rgba(255,255,255,0.80)');
                    p.textSize(32);
                    p.text("Press 'R' to respawn", halfWidth, halfHeight);

                    p.fill('rgba(0,0,0,0.40)');
                    p.fill(255);
                    p.textFont(this.secondaryFont);
                    p.textSize(15);
                    p.text("Press 'H' to hide", halfWidth, halfHeight + 45);
                }
            } 
            
            // Debug text
            if ( this.showDebug ) {
                p.textFont(this.debugFont);
                p.fill(255);
                p.textSize(15);
                p.textAlign('left', 'top');
                p.text("LitBikes " + this.version, 10, 10);
                if ( this.gameJoined ) {
                    p.text(
                        "fps: " + p.frameRate().toFixed(2) + "\n" +
                        "ms: " + this.latency + "ms\n" +
                        "pid: " + this.player.getPid() + "\n" +
                        "pos: " + this.player.getPos().x.toFixed(0) + ", " + this.player.getPos().y.toFixed(0) + "\n" +
                        "dir: "+ this.player.getDir().x + ", " + this.player.getDir().y + "\n" +
                        "spd: "+ this.player.getSpd() + "\n" +
                        "crashed: " + (this.player.isCrashed() ? "yes" : "no") + "\n" +
                        "crashing: " + (this.player.isCrashing() ? "yes" : "no") + "\n" +
                        "colour: " + this.player.getColour() + "\n" +
                        "spectating: " + (this.player.isSpectating() ? "yes" : "no") + "\n" +
                        "round in progress: " + (this.roundInProgress ? "yes" : "no") + "\n" + 
                        "round time left: " + this.roundTimeLeft + "\n" + 
                        "other bikes: " + this.bikes.length + "\n" +
                        "chat message count: " + this.messageCount + "\n"
                    , 10, 30, 300, 500);
                } else {
                    p.text("Game not joined", 10, 30, 300, 500);
                }
            }
        }

        private setup( p : p5 ) {
            this.mainFont = p.loadFont('fonts/3Dventure.ttf');
            this.secondaryFont = p.loadFont('fonts/visitor.ttf');
            this.debugFont = p.loadFont('fonts/larabie.ttf');
            p.createCanvas(this.arena.size, this.arena.size);
        }

    }
    new Game();


}
