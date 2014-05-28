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

  private def changeSnakeMoves(snakeMoves: Set[SnakeMove], aliveSnakesMap: Map[SnakeId, Snake]): Set[Snake] = {
    for {
      snakeMove <- snakeMoves
      snake <- aliveSnakesMap.get(snakeMove.snakeId)
    } yield {
      snake.copy(move = snakeMove.move)
    }
  }
}