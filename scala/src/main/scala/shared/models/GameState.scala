package shared.models
import shared.models.IdTypes.SnakeId
import shared.models.Moves.Move

case class GameState(snakes: GameSnakes = GameSnakes(), foods: GameFoods = GameFoods()) {
  lazy val gameOver: Boolean = snakes.alive.size <= 1

  def getMove(snakeId: SnakeId): Option[Move] = {
    snakes.aliveMap.get(snakeId).map(snake => snake.move)
  }
}