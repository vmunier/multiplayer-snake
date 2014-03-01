package shared.models

import IdTypes.SnakeId
import Moves.Move

case class SnakeMove(snakeId: SnakeId, move: Move)

sealed trait GameNotif {
  def notifType: String
}

case class GameLoopNotif(foods: Set[Block] = Set(), snakes: Set[SnakeMove] = Set(), override val notifType: String = "gameLoop") extends GameNotif {

 def withNewSnakeMove(snakeMove: SnakeMove) = {
    val snakesMap: Map[SnakeId, Move] = snakes.map(snake => (snake.snakeId, snake.move)).toMap + (snakeMove.snakeId -> snakeMove.move)
    val uniqueSnakes = for ((id, move) <- snakesMap) yield {
      SnakeMove(id, move)
    }

    copy(snakes = uniqueSnakes.toSet)
  }
}

case class SnakeWithId(snakeId: SnakeId, snake: Snake)
case class GameInitNotif(snakesWithId: Seq[SnakeWithId], override val notifType: String = "gameInit") extends GameNotif {

  val snakes: Map[SnakeId, Snake] = snakesWithId.flatMap(SnakeWithId.unapply).toMap
}

case class PlayerSnakeIdNotif(playerSnakeId: SnakeId, override val notifType: String = "playerSnakeId") extends GameNotif
