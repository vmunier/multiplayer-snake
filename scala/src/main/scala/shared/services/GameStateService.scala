package shared.services
import shared.models.IdTypes.SnakeId
import shared.models.{Block, GameState, Snake, SnakeMove}

object GameStateService {

  def changeSnakeMove(snakeMove: SnakeMove)(gameState: GameState): GameState = changeSnakeMoves(Set(snakeMove))(gameState)
  def changeSnakeMoves(snakeMoves: Set[SnakeMove])(gameState: GameState): GameState = {
    gameState.copy(snakes =
      gameState.snakes.mergeAliveSnakes(changeSnakeMoves(snakeMoves, gameState.snakes.aliveMap).toSeq))
  }

  def addNewFood(food: Block)(gameState: GameState): GameState = addNewFoods(Set(food))(gameState)
  def addNewFoods(foods: Set[Block])(gameState: GameState): GameState = {
    gameState.copy(foods =
      gameState.foods.copy(available = gameState.foods.available ++ foods))
  }

  def removeDeadSnake(deadSnakeId: SnakeId)(gameState: GameState): GameState = removeDeadSnakes(Set(deadSnakeId))(gameState)
  def removeDeadSnakes(deadSnakeIds: Set[SnakeId])(gameState: GameState): GameState = {
    gameState.copy(snakes = gameState.snakes.addDeadSnakeIds(deadSnakeIds.toSeq: _*))
  }

  private def changeSnakeMoves(snakeMoves: Set[SnakeMove], aliveSnakesMap: Map[SnakeId, Snake]): Set[Snake] = {
    for {
      snakeMove <- snakeMoves
      snake <- aliveSnakesMap.get(snakeMove.snakeId)
    } yield {
      snake.copy(move = snakeMove.move)
    }
  }
}