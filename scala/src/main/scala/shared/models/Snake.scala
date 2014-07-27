package shared.models

import Moves._
import shared.services.BlockService
import shared.models.IdTypes.SnakeId

case class Snake(snakeId: SnakeId, head: Block, tail: List[Block] = List(), nbEatenBlocks: Int = 0, move: Move = Right) {
  val speed = 10 // movement in number of blocks per second
  val blocks = head +: tail
}
