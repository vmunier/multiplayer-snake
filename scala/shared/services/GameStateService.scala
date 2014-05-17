package shared.services
import shared.models.Moves._
import shared.models.GameState
import shared.models.Snake
import shared.models.IdTypes.SnakeId
import shared.models.SnakeMove
import shared.models.SnakeMove
import shared.models.SnakeMove
import shared.models.Block

object GameStateService {

  def moveSnake(gameState: GameState, snakeMove: SnakeMove): GameState = moveSnakes(gameState, Set(snakeMove))
  def moveSnakes(gameState: GameState, snakeMoves: Set[SnakeMove]): GameState = {
    gameState.copy(snakes =
      gameState.snakes.mergeAliveSnakes(changeSnakeMoves(snakeMoves, gameState.snakes.aliveMap).toSeq))
  }

  def addNewFood(gameState: GameState, food: Block): GameState = addNewFoods(gameState, Set(food))
  def addNewFoods(gameState: GameState, foods: Set[Block]): GameState = {
    gameState.copy(foods =
      gameState.foods.copy(available = gameState.foods.available ++ foods))
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