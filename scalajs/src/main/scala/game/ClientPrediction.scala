package game

import scala.scalajs.js.Any.fromFunction0
import scala.scalajs.js.Any.fromLong
import scala.scalajs.js.Number.toDouble

import org.scalajs.dom

import shared.models.GameConstants.GameTickInterval
import shared.models.GameState
import shared.models.IdTypes.GameLoopId
import shared.models.Moves.Move
import shared.models.SnakeMove
import shared.services.GameStateService
import shared.services.TurnService

trait GamePrediction extends PlayerSnakeIdAccess {

  def callOnTick: () => Unit
  private var movesHistory = Map[GameLoopId, Move]()

  def startGamePrediction() = {
    dom.setInterval(callOnTick, GameTickInterval.toMillis).toInt
  }

  def reconcileWithServer(fromGameLoopId: GameLoopId, toGameLoopId: GameLoopId, fromGameState: GameState): GameState = {
    var gameState = fromGameState

    for (gameLoopId <- (fromGameLoopId.id to toGameLoopId.id).map(new GameLoopId(_))) {
      for (snakeMove <- movesHistory.get(gameLoopId)) {
        gameState = GameStateService.changeSnakeMove(SnakeMove(playerSnakeId, snakeMove))(gameState)
      }
      gameState = TurnService.afterTurn(gameState)
    }
    gameState
  }

  def removeGameLoopId(gameLoopId: GameLoopId) = {
    movesHistory -= gameLoopId
  }

  def getMoveFromHistory(gameLoopId: GameLoopId): Option[Move] = {
	movesHistory.get(gameLoopId)
  }

  def registerPlayerMove(gameLoopId: GameLoopId, move: Move) = {
    movesHistory += gameLoopId -> move
  }

  def eraseHistory() = {
    movesHistory = Map()
  }
}