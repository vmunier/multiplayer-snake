package shared.models

object Moves {

  val moves = Seq(Left, Right, Up, Down)

  def fromName: PartialFunction[String, Move] = {
    case Left.name => Left
    case Right.name => Right
    case Up.name => Up
    case Down.name => Down
  }

  sealed trait Move {
    def name: String
  }
  case object Left extends Move {
    val name = "left"
  }
  case object Right extends Move {
    val name = "right"
  }
  case object Up extends Move {
    val name = "up"
  }
  case object Down extends Move {
    val name = "down"
  }
}
