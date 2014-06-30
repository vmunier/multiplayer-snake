package tests

import game.Game
import shared.models.Moves.Move

class TestableGame extends Game {
  override def initJsInterfaces = {}
  override def sendMove(move: Move): Unit = {}
}
