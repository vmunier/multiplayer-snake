package shared.services

import shared.models.GameSnakes
import shared.models.Snake
import shared.models.GameConstants

object CollisionService extends SnakeServiceLayer {

  val afterCollisions: GameSnakes => GameSnakes = {
    afterTailCollisions _ andThen afterHeadCollisions _
  }

  // anticipate collisions for single head snakes that would have crossed a snake without killing it.
  // e.g. : (snake1 with 1 block)-><--(snake2 with 2 blocks)
  // both snake1 and snake2 should be killed. Without this method only snake1 would have been killed.
  def anticipateCollisions(gameSnakes: GameSnakes): GameSnakes = {
    def sameHeadPos(fst: Snake, scd: Snake) = fst.head.pos == scd.head.pos
    val deadSnakes = for {
      both @ Seq(fstSnake, scdSnake) <- gameSnakes.alive.combinations(2)
      if sameHeadPos(snakeService.moveSnake(fstSnake), scdSnake) &&
        sameHeadPos(snakeService.moveSnake(scdSnake), fstSnake)
      snake <- both
    } yield {
      snake
    }

    gameSnakes.addDeadSnakes(deadSnakes.toSeq)
  }

  // two or more snake heads went to the same free block
  private def afterHeadCollisions(gameSnakes: GameSnakes): GameSnakes = {
    val deadSnakes = for {
      (headPos, crossedHeadSnakes) <- gameSnakes.alive.groupBy(_.head.pos)
      if crossedHeadSnakes.size >= 2
      snake <- crossedHeadSnakes
    } yield {
      snake
    }

    gameSnakes.addDeadSnakes(deadSnakes.toSeq)
  }

  // maybe a refactoring semantic problem for handleTailCollisions method
  // hitting a snake's tail
  private def afterTailCollisions(gameSnakes: GameSnakes): GameSnakes = {
    val tailBlocks = gameSnakes.alive.flatMap(_.tail)
    val deadSnakes = gameSnakes.alive.filter(snake => BlockService.findCollision(snake.head, tailBlocks).isDefined)
    gameSnakes.addDeadSnakes(deadSnakes)
  }
}