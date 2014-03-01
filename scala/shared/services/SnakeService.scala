package shared.services

import shared.models.Snake
import shared.models.Position
import shared.models.Block

object SnakeService {
  def moveSnake(snake: Snake, nbBlocksInWidth: Int, nbBlocksInHeight: Int): Snake = {
    val movedTail = moveTailForward(snake)
    val movedHead = moveHead(snake, nbBlocksInWidth, nbBlocksInHeight)
    snake.copy(head = movedHead, tail = movedTail)
  }

  private def moveHead(snake: Snake, nbBlocksInWidth: Int, nbBlocksInHeight: Int): Block = {
    val headPos = snake.head.pos
    val newHeadPos = Position(
      x = (nbBlocksInWidth + headPos.x + MoveService.horizontalCoef(snake.move)) % nbBlocksInWidth,
      y = (nbBlocksInHeight + headPos.y + MoveService.verticalCoef(snake.move)) % nbBlocksInHeight)

    snake.head.copy(pos = newHeadPos)
  }

  private def moveTailForward(snake: Snake): List[Block] = {
    for {
      (newTailPos, tailBlock) <- snake.blocks.init.map(_.pos) zip snake.tail
    } yield {
      tailBlock.copy(pos = newTailPos)
    }
  }
}