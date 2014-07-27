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

  private var _movesHistory = Map[GameLoopId, Move]()

  def movesHistory = _movesHistory

  def removeGameLoopIds(gameLoopIds: Seq[GameLoopId]) = {
    for (id <- gameLoopIds) {
      _movesHistory -= id
    }
  }

  def getMoveFromHistory(gameLoopId: GameLoopId): Option[Move] = {
	_movesHistory.get(gameLoopId)
  }

  /* when a Down move is registered with gameLoopId=1, it means that the first move was Down: the snake has moved down from gameLoopId=0  */
  def registerPlayerMove(gameLoopId: GameLoopId, move: Move) = {
    _movesHistory += gameLoopId -> move
  }

  def eraseHistory() = {
    _movesHistory = Map()
  }
}