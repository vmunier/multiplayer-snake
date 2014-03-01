package shared.models

import java.util.UUID

object IdTypes {
  class GameId(val id: UUID) extends AnyVal {
    override def toString = id.toString
  }

  class SnakeId(val id: Int) extends AnyVal {
    override def toString = id.toString
  }
}

