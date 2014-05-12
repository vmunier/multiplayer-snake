package shared.models

case class GameState(snakes: GameSnakes = GameSnakes(), foods: GameFoods = GameFoods()) {
  def gameOver: Boolean = snakes.alive.size <= 1
}