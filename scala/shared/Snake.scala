package shared

import Moves._

case class Snake(head: Block, tail: List[Block] = List(), blocksEaten: Int = 0, move: Move = Right) {
  val speed = 10 // movement in number of blocks per second
  val blocks = head +: tail

  def moveTailForward(): List[Block] = {
    for {
      (newTailPos, tailBlock) <- blocks.init.map(_.pos) zip tail
    } yield {
      tailBlock.copy(pos = newTailPos)
    }
  }
}
