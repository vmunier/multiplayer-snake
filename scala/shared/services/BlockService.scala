package shared.services

import shared.models.Block
import shared.models.Colors
import shared.models.Position

object BlockService {
  def randomNewBlock(availablePositions: IndexedSeq[Position]): Block = {
    val randomPos = availablePositions((Math.random() * availablePositions.size).toInt)
    Block(randomPos, Colors.nextColor.rgb)
  }

  def findCollision(snakeBlock: Block, blocks: Iterable[Block]): Option[Block] = {
    blocks.filter(block => block.pos == snakeBlock.pos).headOption
  }
}