package game

import scala.scalajs.js
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

trait GameVars extends mutable.GameMutations with GameConstants {

  lazy val BlockSize = Math.min(
    (Canvas.windowHeight / NbBlocksInHeight).toInt,
    (Canvas.windowWidth / NbBlocksInWidth).toInt)

  var playerSnakeId: SnakeId = new SnakeId(0)

  g.window.game = new js.Object

  // il faut gameInitNotif contienne aussi le snakeId pour chacun des snakes
  g.window.game.receiveGameInitNotif = (notif: JsGameInitNotif) => {
    val canvas = Canvas.init()
    val gameInitNotif = GameNotifParser.parseGameInitNotif(notif)
    snakes = gameInitNotif.snakes.map(s => s.snakeId -> s).toMap
  }

  g.window.game.receivePlayerSnakeId = (notif: JsPlayerSnakeIdNotif) => {
    playerSnakeId = new SnakeId(notif.playerSnakeId)
  }
}

object Game extends GameVars {
  def onGameLoopNotif(gameLoopNotif: GameLoopNotif) = {
    updateMove(gameLoopNotif.snakes)
    updateFood(gameLoopNotif.foods)

    super.moveSnakes()
  }

  def render() = {
    val playerNbEatenBlocks = (snakes ++ losingSnakes).get(playerSnakeId).map(_.nbEatenBlocks).getOrElse(0)
    val blocks = (snakes.values.flatMap(_.blocks) ++ foods ++ foodsInDigestion).toSeq
    val gameLost = losingSnakes.contains(playerSnakeId)
    val maybeSnakeHead = snakes.get(playerSnakeId).map(_.head)
    Canvas.render(playerNbEatenBlocks, maybeSnakeHead, blocks, gameOver, gameLost)
  }

  private def updateMove(snakeMoves: Set[SnakeMove]): Unit = {
    for {
      SnakeMove(snakeId, newMove) <- snakeMoves
      snake <- snakes.get(snakeId)
    } {
      snakes += snakeId -> snake.copy(move = newMove)
    }
  }

  private def updateFood(newFoods: Set[Block]): Unit = {
    for (newFood <- newFoods) {
      foods += newFood
    }
  }

  def main(): Unit = {
    GameLoop(onGameLoopNotif, render).start()
  }
}
