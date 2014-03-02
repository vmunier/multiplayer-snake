@(gameUUID: java.util.UUID, maybeCreatorUUID: Option[java.util.UUID])(implicit request: RequestHeader)

// The Scala.js code should provide implementations for the following functions:
// - window.game.receiveGameInitNotif(object)
// - window.game.receiveGameNotif(object)
// - window.game.receivePlayerSnakeId(integer)
(function() {
  var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
  var gameSocket = new WS("@routes.Application.joinGame(gameUUID).webSocketURL()");

  var sendMove = function(move) {
    gameSocket.send(JSON.stringify({move: move}));
  };

  @maybeCreatorUUID.map { creatorUUID =>
    $(".startGameBtn").click(function(){
      $.ajax({url:"@routes.Application.startGame(gameUUID, creatorUUID)"});
      setTimeout(function() {
        $('.startGameMsg').show();
      }, 500);
    });
  }
  var receiveEvent = function(event) {
    var data = JSON.parse(event.data);
    if(data.error) {
      gameSocket.close();
    } else if (data.notifType == "gameInit") {
      window.game.receiveGameInitNotif(data);
    } else if (data.notifType == "playerSnakeId") {
      window.game.receivePlayerSnakeId(data);
    } else {
      window.game.receiveGameLoopNotif(data);
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
