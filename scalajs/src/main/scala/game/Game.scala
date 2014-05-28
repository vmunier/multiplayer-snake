package game

import scala.scalajs.js
import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.Any.fromFunction1
import scala.scalajs.js.Any.fromInt
import scala.scalajs.js.Any.fromString
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.Number.toDouble
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom.WebSocket

import shared.models.GameConstants.NbBlocksInHeight
import shared.models.GameConstants.NbBlocksInWidth
import shared.models.GameLoopNotif
import shared.models.GameSnakes
import shared.models.GameState
import shared.models.IdTypes.GameLoopId
import shared.models.IdTypes.GameLoopIdToLong
import shared.models.IdTypes.SnakeId
import shared.models.Moves.Move
import shared.models.Moves.Right
import shared.models.SnakeMove
import shared.services.GameStateService
import shared.services.MoveService
import shared.services.TurnService

trait PlayerSnakeIdAccess {
  def playerSnakeId: SnakeId
}

trait GameVars extends PlayerSnakeIdAccess {
  lazy val BlockSize = Math.min(
    (Canvas.windowHeight / NbBlocksInHeight).toInt,
    (Canvas.windowWidth / NbBlocksInWidth).toInt)

  var _gameState = GameState()
  def gameState = _gameState
  def gameState_=(newGameState: GameState) {
    _gameState = newGameState
  }

  var lastMove: Move = Right

  var playerSnakeId: SnakeId = new SnakeId(0)
  var _savedGameState = gameState
  var lastGameLoopId = new GameLoopId(0)

  def saveGameState(gameState: GameState) {
    _savedGameState = gameState
  }

  def getSavedGameState = _savedGameState
}

@JSExport
object Game extends GameVars with GamePrediction {

  override val callOnTick: () => Unit = () => {
    val changeSnakeMove = GameStateService.changeSnakeMove(SnakeMove(playerSnakeId, lastMove)) _
    val changedGameState = (changeSnakeMove andThen TurnService.afterTurn _)(gameState)
    // if a snake is dead, we prefer waiting confirmation of the server and not apply client prediction
    if (changedGameState.snakes.alive.size == gameState.snakes.alive.size) {
      gameState = changedGameState
      lastGameLoopId = new GameLoopId(lastGameLoopId + 1)
      registerPlayerMove(lastGameLoopId, lastMove)
    }
  }

  def onGameLoopNotif(serverLoopNotif: GameLoopNotif) = {
    val changeSnakeMoves = GameStateService.changeSnakeMoves(serverLoopNotif.snakes) _
    val addNewFoods = GameStateService.addNewFoods(serverLoopNotif.foods) _
    val serverGameState = (changeSnakeMoves andThen addNewFoods andThen (TurnService.afterTurn _))(getSavedGameState)
    val serverLoopId = serverLoopNotif.gameLoopId

    saveGameState(serverGameState)
    gameState = serverGameState

    if (lastGameLoopId < serverLoopId) {
      lastGameLoopId = serverLoopId
      for (snake <- serverGameState.snakes.aliveMap.get(playerSnakeId)) {
        lastMove = snake.move
      }
    } else {
      val serverSnakeMove = serverGameState.snakes.aliveMap.get(playerSnakeId).map(_.move)
      val sameMove = super.getMoveFromHistory(serverLoopId) == serverSnakeMove
      if (sameMove) {
        gameState = super.reconcileWithServer(new GameLoopId(serverLoopId + 1), new GameLoopId(lastGameLoopId), serverGameState)
        super.removeGameLoopId(serverLoopId)
      } else {
        eraseHistory()
        for (move <- serverSnakeMove) {
          lastMove = move
        }
        lastGameLoopId = serverLoopId
      }
    }
  }

  def render() = {
    val gameSnakes = gameState.snakes

    val playerNbEatenBlocks = (gameSnakes.allMap).get(playerSnakeId).map(_.nbEatenBlocks).getOrElse(0)
    val blocks = gameSnakes.alive.flatMap(_.blocks) ++ gameState.foods.all
    val gameLost = gameSnakes.dead.exists(_.snakeId == playerSnakeId)
    val maybeSnakeHead = gameSnakes.aliveMap.get(playerSnakeId).map(_.head)
    Canvas.render(playerNbEatenBlocks, maybeSnakeHead, blocks, gameState.gameOver, gameLost)
  }

  private val renderLoop: () => Unit = () => {
    g.window.requestAnimationFrame(renderLoop)
    render()
  }

  def initJsInterfaces() = {
    g.window.game = new js.Object
    g.window.game.receiveGameInitNotif = (notif: JsGameInitNotif) => {
      val canvas = Canvas.init()
      val gameInitNotif = GameNotifParser.parseGameInitNotif(notif)
      gameState = gameState.copy(snakes = GameSnakes(gameInitNotif.snakes))
      saveGameState(gameState)
      renderLoop()
      super.startGamePrediction()
    }

    g.window.game.receiveGameLoopNotif = (x: JsGameLoopNotif) => onGameLoopNotif(GameNotifParser.parseGameLoopNotif(x))

    g.window.game.receivePlayerSnakeId = (notif: JsPlayerSnakeIdNotif) => {
      playerSnakeId = new SnakeId(notif.playerSnakeId)
    }

    g.window.game.receiveDisconnectedSnake = (notif: JsDisconnectedSnakeNotif) => {
      gameState = gameState.copy(
        snakes = gameState.snakes.addDeadSnakeIds(new SnakeId(notif.disconnectedSnakeId)))
      saveGameState(gameState)
    }
  }
  initJsInterfaces()

  def sendMove(gameSocket: WebSocket, move: Move): Unit = {
    gameSocket.send(s"""{"move": "${move.name}"}""")
  }

  @JSExport
  def main(gameSocket: WebSocket): Unit = {
    val keyboard = new Keyboard()
    keyboard.registerEventListeners()

    keyboard.onMove { move =>
      sendMove(gameSocket, move)
      for {
        snake <- gameState.snakes.aliveMap.get(playerSnakeId)
        if MoveService.isValidMove(snake, move)
      } {
        lastMove = move
      }
    }
  }
}
