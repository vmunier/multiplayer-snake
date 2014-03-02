package shared.models.mutable

import shared.models.IdTypes._
import shared.models.GameNotif
import shared.models.Snake
import shared.models.GameConstants
import shared.models.Moves
import shared.services.MoveService
import shared.services.BlockService
import shared.services.SnakeService

trait GameMutations extends GameFood with GameConstants {
  var snakes = Map[SnakeId, Snake]()
  var losingSnakes = Map[SnakeId, Snake]()
  var gameOver = false

  val snakeService = SnakeService(NbBlocksInWidth, NbBlocksInHeight)

  def moveSnakes(): Unit = {
    if (gameOver) return

    anticipateCollisions()
    moves()
    // handleCollisions() for all snakes
    handleCollisions()

    eatFoods()
    foodsReachingQueue()

    gameOver = snakes.size <= 1
  }

  // anticipate collisions for single head snakes that would have crossed a snake without killing it.
  // e.g. : (snake1 with 1 block)-><--(snake2 with 2 blocks)
  // both snake1 and snake2 should be killed. Without this method only snake1 would have been killed.
  private def anticipateCollisions() = {
    def sameHeadPos(fst: Snake, scd: Snake) = fst.head.pos == scd.head.pos
    for {
      Seq(fstSnake, scdSnake) <- snakes.values.toSeq.combinations(2)
      if sameHeadPos(snakeService.moveSnake(fstSnake), scdSnake) &&
        sameHeadPos(snakeService.moveSnake(scdSnake), fstSnake)
    } {
      killSnake(fstSnake)
      killSnake(scdSnake)
    }
  }

  private def moves() = {
    for ((snakeId, snake) <- snakes) {
      val movedSnake = snakeService.moveSnake(snake)
      snakes += snakeId -> movedSnake
    }
  }

  private def eatFoods() = {
    for {
      (snakeId, snake) <- snakes
      eatenFood <- BlockService.findCollision(snake.head, foods)
    } {
      startDigestionForFood(eatenFood)
      snakes += snakeId -> snake.copy(nbEatenBlocks = snake.nbEatenBlocks + 1)
    }
  }

  private def foodsReachingQueue() = {
    for {
      (snakeId, snake) <- snakes
      foodReachingEndQueue <- BlockService.findCollision(snake.blocks.last, foodsInDigestion)
    } {
      snakes += snakeId -> snake.copy(tail = snake.tail ++ Seq(foodReachingEndQueue))
      endDigestionForFood(foodReachingEndQueue)
    }
  }

  private def killSnake(snake: Snake) = {
    snakes -= snake.snakeId
    losingSnakes += snake.snakeId-> snake
  }

  private def handleCollisions() = {
    // two or more snake heads went to the same free block
    for {
      (headPos, crossedHeadSnakes) <- snakes.values.groupBy(_.head.pos)
      if crossedHeadSnakes.size > 2
      snake <- crossedHeadSnakes
    } {
      killSnake(snake)
    }

    // hitting a snake's tail
    for ((snakeId, snake) <- snakes) {
      val tailBlocks = snakes.values.flatMap(_.tail)
      if (BlockService.findCollision(snake.head, tailBlocks).isDefined) {
        killSnake(snake)
      }
    }
  }

}