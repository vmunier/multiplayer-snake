@(gameUUID: java.util.UUID)(implicit request: RequestHeader)

// The Scala.js code should provide implementations for the following functions:
// - window.game.receiveGameNotif(object)
// - window.game.receivePlayerSnakeId(integer)
(function() {
  var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
  var gameSocket = new WS("@routes.Application.joinGame(gameUUID).webSocketURL()");

  var sendMove = function(move) {
    console.log("send " + move);
    gameSocket.send(JSON.stringify({move: move}));
  };

  var receiveEvent = function(event) {
    var data = JSON.parse(event.data);

    if(data.error) {
      gameSocket.close();
    } else if (data.playerSnakeId !== undefined) {
      window.game.receivePlayerSnakeId(data.playerSnakeId);
    } else {
      window.game.receiveGameNotif(data);
    }
  };

  addEventListener("keydown", function(e) {
    if (e.keyCode == 37) sendMove('left');
    else if (e.keyCode == 38) sendMove('up');
    else if (e.keyCode == 39) sendMove('right');
    else if (e.keyCode == 40) sendMove('down');
  }, false);

  gameSocket.onmessage = receiveEvent;
})();