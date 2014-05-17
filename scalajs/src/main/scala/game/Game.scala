package game

import scala.scalajs.js
import js.annotation.JSExport
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.extensions._
import org.scalajs.dom.HTMLCanvasElement
import shared.models.mutable
import shared.models.Snake
import shared.models.Block
import shared.models.Position
import shared.models.Colors
import shared.models.Moves._
import shared.models.GameConstants
import shared.models.GameNotif
import shared.models.GameLoopNotif
import shared.models.IdTypes.SnakeId
import shared.models.SnakeMove
import shared.services.SnakeService
import shared.services.BlockService
import org.scalajs.dom.WebSocket
import scala.scalajs.js.JSON
import org.scalajs.dom.MessageEvent
import shared.models.GameState
import shared.models.GameSnakes
import shared.models.GameConstants._
import shared.services.TurnService

trait GameVars {
  lazy val BlockSize = Math.min(
    (Canvas.windowHeight / NbBlocksInHeight).toInt,
    (Canvas.windowWidth / NbBlocksInWidth).toInt)

  var gameState = GameState()
  var playerSnakeId: SnakeId = new SnakeId(0)
  var _savedGameState = gameState

  def saveGameState(gameState: GameState) {
    _savedGameState = gameState
  }

  def getSavedGameState = _savedGameState
}

@JSExport
object Game extends GameVars with GamePrediction {

  override def callOnTick(): Unit = {
    gameState = TurnService.afterTurn(gameState)
  }

  def onGameLoopNotif(gameLoopNotif: GameLoopNotif) = {
    super.stopGamePrediction()
    gameState = getSavedGameState
    updateOnGameLoopNotif(gameLoopNotif)
    saveGameState(gameState)
    super.startGamePrediction()
  }

  private def updateOnGameLoopNotif(gameLoopNotif: GameLoopNotif) = {
    gameState = gameState.copy(snakes =
      gameState.snakes.mergeAliveSnakes(changeSnakeMoves(gameLoopNotif.snakes).toSeq))

    gameState = gameState.copy(foods =
      gameState.foods.copy(available = gameState.foods.available ++ gameLoopNotif.foods))

    callOnTick()
  }

  private def changeSnakeMoves(snakeMoves: Set[SnakeMove]): Set[Snake] = {
    for {
      SnakeMove(snakeId, newMove) <- snakeMoves
      newSnake <- changeSnakeMove(snakeId, newMove, gameState.snakes.aliveMap)
    } yield {
      newSnake
    }
  }

  private def changeSnakeMove(snakeId: SnakeId, move: Move, aliveSnakesMap: Map[SnakeId, Snake]): Option[Snake] = {
    for (snake <- aliveSnakesMap.get(snakeId)) yield {
      snake.copy(move = move)
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

      gameState = gameState.copy(snakes =
        gameState.snakes.mergeAliveSnakes(changeSnakeMove(playerSnakeId, move, gameState.snakes.aliveMap).toSeq))
    }
  }
}
