package game

import shared.models.IdTypes.GameLoopId
import shared.models.Moves.Move

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