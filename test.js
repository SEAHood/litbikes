var Server;
(function (Server) {
    var express = require('express');
    var app = express();
    var http = require('http').Server(app);
    var io = require('socket.io')(http);
    var _ = require('underscore');
})(Server || (Server = {}));
//# sourceMappingURL=test.js.map