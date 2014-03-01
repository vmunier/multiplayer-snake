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
import shared.models.IdTypes.SnakeId
import shared.models.SnakeMove
import shared.services.SnakeService
import shared.services.BlockService

trait GameVars extends mutable.GameFood with GameConstants {

  lazy val BlockSize = Math.min(
    (Canvas.windowHeight / NbBlocksInHeight).toInt,
    (Canvas.windowWidth / NbBlocksInWidth).toInt)

  val canvas = Canvas.init()

  var snakes: Map[SnakeId, Snake] = Map()
  var playerSnakeId: SnakeId = new SnakeId(0)

  var gameOver = false
  var win = false

  g.window.game = new js.Object

  g.window.game.receivePlayerSnakeId = (snakeId: js.Number) => {
    println("received snakeId : " + snakeId)
    playerSnakeId = new SnakeId(snakeId.toInt)
  }

}

object Game extends GameVars {
  Keyboard.init()

  def update(gameNotif: GameNotif) = {
    if (gameOver) {
      // do nothing
    } else {
      updateMove(gameNotif.snakes)
      updateFood(gameNotif.foods)

      for ((snakeId, snake) <- snakes) {
        moveSnake(snakeId, snake)
        handleCollisions(snakeId, snake)
      }
    }
  }

  def render() = {
    Canvas.render(snakes.get(playerSnakeId).map(_.nbEatenBlocks).getOrElse(0), (snakes.values.flatMap(_.blocks) ++ foods ++ foodsInDigestion).toSeq)
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

  private def handleCollisions(snakeId: SnakeId, snake: Snake) = {
    if (snake.bitesItsQueue) {
      // gameOver for snake
    } else {
      // digested food
      for (eatenFood <- BlockService.findCollision(snake.head, foods)) {
        startDigestionForFood(eatenFood)
        snakes += snakeId -> snake.copy(nbEatenBlocks = snake.nbEatenBlocks + 1)
      }

      // food reaching queue
      for (foodReachingEndQueue <- BlockService.findCollision(snake.blocks.last, foodsInDigestion)) {
        snakes += snakeId -> snake.copy(tail = snake.tail ++ Seq(foodReachingEndQueue))
        endDigestionForFood(foodReachingEndQueue)
      }
    }
  }

  private def moveSnake(snakeId: SnakeId, snake: Snake) = {
    println("in moveSnake")
    val movedSnake = SnakeService.moveSnake(snake, NbBlocksInWidth, NbBlocksInHeight)
    println("movedSnake : " + movedSnake)
    snakes += snakeId -> movedSnake
  }

  def main(): Unit = {
    GameLoop(update, render).start()
  }
}
