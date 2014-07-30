package models

import shared.models.GameState
import shared.models.IdTypes.GameLoopId

case class GameLoopState(gameState: GameState = GameState(), gameLoopId: GameLoopId = new GameLoopId(0))