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

case class GameInitNotif(snakes: Seq[Snake], override val notifType: String = "gameInit") extends GameNotif

case class PlayerSnakeIdNotif(playerSnakeId: SnakeId, override val notifType: String = "playerSnakeId") extends GameNotif

case class DisconnectedSnakeNotif(disconnectedSnakeId: SnakeId, override val notifType: String = "disconnectedSnake") extends GameNotif

case class Heartbeat(override val notifType: String = "heartbeat") extends GameNotif
