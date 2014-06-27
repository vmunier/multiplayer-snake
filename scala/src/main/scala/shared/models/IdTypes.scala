package shared.models

import java.util.UUID

object IdTypes {
  class GameId(val id: UUID) extends AnyVal {
    override def toString = id.toString
  }

  class SnakeId(val id: Int) extends AnyVal {
    override def toString = id.toString
  }

  class GameLoopId(val id: Int) extends AnyVal {
    override def toString = id.toString
  }

  implicit val SnakeIdToInt = (snakeId: SnakeId) => snakeId.id
  implicit val GameLoopIdToLong = (gameLoopId: GameLoopId) => gameLoopId.id
}

