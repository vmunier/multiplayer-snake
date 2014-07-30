package game

import models.GameLoopState
import org.scalajs.dom.WebSocket
import services.GameLoopStateService
import shared.models.IdTypes.{GameLoopId, GameLoopIdToLong, SnakeId}
import shared.models.Moves.Move
import shared.models.{GameConstants, GameLoopNotif, GameSnakes, GameState, SnakeMove}
import shared.services.{GameStateService, MoveService, TurnService}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

trait PlayerSnakeIdAccess {
  def playerSnakeId: SnakeId
}

trait GameVars extends PlayerSnakeIdAccess {
  var serverState = GameLoopState()
  var clientState = GameLoopState()

  var playerSnakeId: SnakeId = new SnakeId(0)

  var gameSocket: WebSocket = _
  var nextMove: Move = _
}

class Game extends GameVars with GamePrediction {

  def onGameTick() = {
    val move = nextMove
    if (moveCanBeChanged(move)) {
      sendMove(move)
      val changedClientGameState = GameStateService.changeSnakeMove(SnakeMove(playerSnakeId, move))(clientState.gameState)
      clientState = clientState.copy(gameState = changedClientGameState)
      super.registerPlayerMove(new GameLoopId(clientState.gameLoopId + 1), move)
    }

    val newGameState = TurnService.afterTurn(clientState.gameState)
    updateClientStateIfNoDeath(newGameState)
  }

  private def moveCanBeChanged(move: Move): Boolean = {
    clientState.gameState.snakes.aliveMap.get(playerSnakeId).exists(snake =>
      MoveService.isValidMove(snake, nextMove) && snake.move != nextMove)
  }

  private def updateClientStateIfNoDeath(newClientGameState: GameState) = {
    // if a snake is dead, we prefer waiting confirmation of the server and not apply client prediction
    if (newClientGameState.snakes.alive.size == clientState.gameState.snakes.alive.size) {
      clientState = clientState.copy(gameState = newClientGameState, new GameLoopId(clientState.gameLoopId + 1))
    }
  }

  /* it's a def because it depends on playerSnakeId which has to be received from the server */
  def reconcileWithServer = GameLoopStateService.reconcileWithServer(playerSnakeId) _

  def onGameLoopNotif(serverLoopNotif: GameLoopNotif) = {
    val prevServerGameLoopId = serverState.gameLoopId
    serverState = GameLoopStateService.applyGameLoopNotif(serverState, serverLoopNotif)

    val clientGameState = reconcileWithServer(serverState, clientState.gameLoopId, movesHistory)
    clientState = clientState.copy(gameState = clientGameState)
    super.removeGameLoopIds((prevServerGameLoopId.id to serverState.gameLoopId.id).map(new GameLoopId(_)))
  }

  def killSnake(snakeId: SnakeId) = {
    val clientGameState = clientState.gameState
    clientState = clientState.copy(gameState = clientGameState.copy(
      snakes = clientGameState.snakes.addDeadSnakeIds(snakeId)))
  }

  def render() = {
    val clientGameState = clientState.gameState
    val gameSnakes = clientGameState.snakes

    val playerNbEatenBlocks = gameSnakes.allMap.get(playerSnakeId).map(_.nbEatenBlocks).getOrElse(0)
    val blocks = gameSnakes.alive.flatMap(_.blocks) ++ clientGameState.foods.all
    val gameLost = gameSnakes.dead.exists(_.snakeId == playerSnakeId)
    val maybeSnakeHead = gameSnakes.aliveMap.get(playerSnakeId).map(_.head)
    Canvas.render(playerNbEatenBlocks, maybeSnakeHead, blocks, clientGameState.gameOver, gameLost)
  }

  var last = js.Date.now()
  private val renderLoop: () => Unit = () => {
    val gameTickInterval = GameConstants.GameTickInterval.toMillis.toInt
    val now = js.Date.now()
    val nbLoops = (now - last).toInt / gameTickInterval
    if (nbLoops >= 1) {
      for (_ <- 1 to nbLoops) {
        onGameTick()
      }
      last += nbLoops * gameTickInterval
    }

    g.window.requestAnimationFrame(renderLoop)
    render()
  }

  def initJsInterfaces() = {
    g.window.game = new js.Object
    g.window.game.receiveGameInitNotif = (notif: JsGameInitNotif) => {
      Canvas.init()
      val gameInitNotif = GameNotifParser.parseGameInitNotif(notif)
      serverState = GameLoopState(GameState(snakes = GameSnakes(gameInitNotif.snakes)))
      clientState = serverState

      for (snakeMove <- clientState.gameState.getMove(playerSnakeId)) {
        nextMove = snakeMove
        super.registerPlayerMove(serverState.gameLoopId, snakeMove)
      }

      last = js.Date.now()
      renderLoop()
    }

    g.window.game.receiveGameLoopNotif = (x: JsGameLoopNotif) => onGameLoopNotif(GameNotifParser.parseGameLoopNotif(x))

    g.window.game.receivePlayerSnakeId = (notif: JsPlayerSnakeIdNotif) => {
      playerSnakeId = new SnakeId(notif.playerSnakeId)
    }

    g.window.game.receiveDisconnectedSnake = (notif: JsDisconnectedSnakeNotif) => {
      clientState = clientState.copy(gameState = GameStateService.removeDeadSnake(new SnakeId(notif.disconnectedSnakeId))(clientState.gameState))
      serverState = serverState.copy(gameState = GameStateService.removeDeadSnake(new SnakeId(notif.disconnectedSnakeId))(serverState.gameState))
    }

    g.window.game.setGameSocket = (socket: WebSocket) => {
      gameSocket = socket
    }
  }

  initJsInterfaces()

  def sendMove(move: Move): Unit = {
    gameSocket.send( s"""{"move": "${move.name}"}""")
  }
}

object GameApp extends js.JSApp {
  val game = new Game()

  def main(): Unit = {
    val keyboard = new Keyboard()
    keyboard.registerEventListeners()
    keyboard.onMove { move =>
      game.nextMove = move
    }
  }
}
