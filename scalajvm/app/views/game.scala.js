@(gameUUID: java.util.UUID, maybeCreatorUUID: Option[java.util.UUID])(implicit request: RequestHeader)

// The Scala.js code should provide implementations for the following functions:
// - window.game.receiveGameInitNotif(object)
// - window.game.receiveGameNotif(object)
// - window.game.receivePlayerSnakeId(integer)
(function() {
  var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
  var gameSocket = new WS("@routes.Application.joinGame(gameUUID, maybeCreatorUUID).webSocketURL()");

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
    } else if (data.notifType == "heartbeat") {
      // do nothing
    } else if (data.notifType == "disconnectedSnake") {
      window.game.receiveDisconnectedSnake(data);
    } else {
      window.game.receiveGameLoopNotif(data);
    }
  };

  gameSocket.onmessage = receiveEvent;

  Game().main(gameSocket);
})();
