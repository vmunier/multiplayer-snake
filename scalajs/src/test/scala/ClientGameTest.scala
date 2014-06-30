package tests

import shared.models.IdTypes.GameLoopId
import shared.models.Moves._
import utest._
import shared.models.{SnakeMove, Position, GameLoopNotif}
import scala.scalajs.test.JasmineTest


object ClientGameTest extends JasmineTest with BaseGameTest {

  describe("A snake") {
    it("should move properly after a client tick") {
      game.clientState = firstLoopState
      game.onGameTick

      val movedPlayerSnake = getSnakeFromState(game.clientState, playerSnake.snakeId).get
      assert(movedPlayerSnake.head.pos == Position(3, 2))
    }
  }

  describe("The server reconciliation") {
    it("should have no effect when the client state matches the server state") {
      game.clientState = firstLoopState
      game.serverState = firstLoopState

      val nextMove = Down

      val numTickWhenToChangeMove = 4
      for (numTick <- 1 to 8) {
        if (numTick ==numTickWhenToChangeMove) {
          game.nextMove = nextMove

        }
        game.onGameTick
      }

      val savedClientState = game.clientState
      game.onGameLoopNotif(GameLoopNotif(new GameLoopId(numTickWhenToChangeMove), snakeMoves = Set(SnakeMove(playerSnake.snakeId, nextMove))))
      assert(savedClientState == game.clientState)
    }
  }
}


