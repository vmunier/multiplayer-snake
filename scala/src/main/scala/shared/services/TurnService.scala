package shared.services

import shared.models.GameState

object TurnService extends SnakeServiceLayer {
  def afterTurn(gameState: GameState): GameState = {
    if (gameState.gameOver) {
      gameState
    } else {
      val gameSnakes = CollisionService.anticipateCollisions(gameState.snakes)
      val movedSnakes = snakeService.moveSnakes(gameSnakes.alive)
      val gameSnakesAfterCollisions = CollisionService.afterCollisions(gameSnakes.copy(alive = movedSnakes))
      val (satisfiedSnakes, foods) = FoodService.eatAndDigested(gameSnakesAfterCollisions.alive, gameState.foods)
      gameState.copy(
        snakes = gameSnakesAfterCollisions.copy(alive = satisfiedSnakes),
        foods = foods)
    }
  }
}