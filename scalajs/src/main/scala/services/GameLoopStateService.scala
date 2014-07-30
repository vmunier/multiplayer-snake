package services

import models.GameLoopState
import shared.models.IdTypes.{GameLoopId, SnakeId}
import shared.models.Moves.Move
import shared.models.{GameLoopNotif, GameState, SnakeMove}
import shared.services.{GameStateService, TurnService}

object GameLoopStateService {

  def applyGameLoopNotif(serverState: GameLoopState, serverLoopNotif: GameLoopNotif): GameLoopState = {
    if (!serverLoopNotif.deadSnakes.isEmpty) {
      println("A dead snakes receives on the client side")
    }
    val removeDeadSnakes = GameStateService.removeDeadSnakes(serverLoopNotif.deadSnakes) _
    val changeSnakeMoves = GameStateService.changeSnakeMoves(serverLoopNotif.snakeMoves) _
    val addNewFoods = GameStateService.addNewFoods(serverLoopNotif.foods) _

    val nbTurnsWithoutChange = serverLoopNotif.gameLoopId - serverState.gameLoopId.id - 1

    val turnsWithoutChange = List.fill(nbTurnsWithoutChange)(TurnService.afterTurn _).foldLeft(identity[GameState] _)(_ andThen _)
    val serverGameState = (turnsWithoutChange andThen removeDeadSnakes andThen changeSnakeMoves andThen addNewFoods andThen (TurnService.afterTurn _))(serverState.gameState)

    GameLoopState(serverGameState, serverLoopNotif.gameLoopId)
  }

  def reconcileWithServer(playerSnakeId: SnakeId)(serverState: GameLoopState, clientGameLoopId: GameLoopId, movesHistory: Map[GameLoopId, Move]): GameState = {
    var gameState = serverState.gameState

    for (gameLoopId <- ((serverState.gameLoopId.id + 1) to clientGameLoopId.id).map(new GameLoopId(_))) {
      for (snakeMove <- movesHistory.get(gameLoopId)) {
        gameState = GameStateService.changeSnakeMove(SnakeMove(playerSnakeId, snakeMove))(gameState)
      }
      gameState = TurnService.afterTurn(gameState)
    }
    gameState
  }
}