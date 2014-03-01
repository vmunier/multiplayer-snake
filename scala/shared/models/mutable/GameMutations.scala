package shared.models.mutable

import shared.models.IdTypes._
import shared.models.GameNotif
import shared.models.Snake
import shared.models.GameConstants
import shared.services.MoveService
import shared.services.BlockService
import shared.services.SnakeService

trait GameMutations extends GameFood with GameConstants {
  var snakes = Map[SnakeId, Snake]()
  var losingSnakes = Map[SnakeId, Snake]()
  var gameOver = false

  def moveSnakes(): Unit = {
    if (gameOver) return

    for ((snakeId, snake) <- snakes) {
      val movedSnake = SnakeService.moveSnake(snake, NbBlocksInWidth, NbBlocksInHeight)
      snakes += snakeId -> movedSnake

      handleCollisions(snakeId)
    }

    gameOver = snakes.size <= 1
  }

  private def handleCollisions(snakeId: SnakeId) = {
    // this local snake var is used to avoid retrieving every time the updated snake in the snakes Map
    var snake = snakes(snakeId)

    val otherSnakeBlocks = (snakes - snakeId).values.flatMap(_.blocks)

    if (BlockService.findCollision(snake.head, snake.tail ++ otherSnakeBlocks).isDefined) {
      // gameOver for snake
      snakes -= snakeId
      losingSnakes += snakeId -> snake
    } else {
      // digested food
      for (eatenFood <- BlockService.findCollision(snake.head, foods)) {
        startDigestionForFood(eatenFood)
        snake = snake.copy(nbEatenBlocks = snake.nbEatenBlocks + 1)
      }

      // food reaching queue
      for (foodReachingEndQueue <- BlockService.findCollision(snake.blocks.last, foodsInDigestion)) {
        snake = snake.copy(tail = snake.tail ++ Seq(foodReachingEndQueue))
        endDigestionForFood(foodReachingEndQueue)
      }
      snakes += snakeId -> snake
    }
  }

}