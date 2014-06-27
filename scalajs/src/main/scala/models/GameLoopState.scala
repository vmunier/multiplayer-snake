package models

import shared.models.IdTypes.GameLoopId
import shared.models.GameState
import shared.models.GameSnakes

case class GameLoopState(gameState: GameState = GameState(), gameLoopId: GameLoopId = new GameLoopId(0)) {
  // TMP: used for debug
  def tmpDiff(other: GameLoopState): GameState = {
    GameState(GameSnakes(
      gameState.snakes.alive.diff(other.gameState.snakes.alive),
      gameState.snakes.dead.diff(other.gameState.snakes.dead)))
  }
}