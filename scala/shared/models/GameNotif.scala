package shared.models

import IdTypes.SnakeId
import Moves.Move

case class GameNotif(foods: Set[Block] = Set(), snakes: Set[SnakeMove] = Set()) {
  def withNewSnakeMove(snakeMove: SnakeMove) = {
    val snakesMap: Map[SnakeId, Move] = snakes.map(snake => (snake.snakeId, snake.move)).toMap + (snakeMove.snakeId -> snakeMove.move)
    val uniqueSnakes = for ((id, move) <- snakesMap) yield {
      SnakeMove(id, move)
    }

    copy(snakes = uniqueSnakes.toSet)
  }
}

case class SnakeMove(snakeId: SnakeId, move: Move)

