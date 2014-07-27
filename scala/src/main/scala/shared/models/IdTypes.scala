package shared.models

import java.util.UUID

object IdTypes {
  class GameId(val id: UUID) extends AnyVal {
    override def toString = id.toString
  }

  class SnakeId(val id: Int) extends AnyVal {
    override def toString = id.toString
  }

  /* GameLoopId is one based. GameLoopId=NbTotalMoves from the game start. When GameLoopId=0, no game tick has been triggered */
  class GameLoopId(val id: Int) extends AnyVal {
    override def toString = id.toString
  }

  implicit val SnakeIdToInt = (snakeId: SnakeId) => snakeId.id
  implicit val GameLoopIdToLong = (gameLoopId: GameLoopId) => gameLoopId.id
}

