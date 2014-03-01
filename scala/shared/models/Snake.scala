package shared.models

import Moves._

case class Snake(head: Block, tail: List[Block] = List(), nbEatenBlocks: Int = 0, move: Move = Right) {
  val speed = 10 // movement in number of blocks per second
  val blocks = head +: tail

  def bitesItsQueue: Boolean = {
    tail.map(_.pos).contains(head.pos)
  }
}
