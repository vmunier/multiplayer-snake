package tests

import shared.models.IdTypes.GameLoopId
import shared.models.Moves._
import utest._
import shared.models.{SnakeMove, Position, GameLoopNotif}
import scala.scalajs.test.JasmineTest


object ClientGameTest extends BaseGameTest {

  val NbTicks = 5

  trait ClientGameScope {
    game.clientState = firstLoopState
    game.serverState = firstLoopState
    val nextMove = Down
  }

  describe("A snake") {
    it("should move properly after a client tick")(new ClientGameScope {
      game.onGameTick

      val movedPlayerSnake = getSnakeFromState(game.clientState, playerSnake.snakeId).get
      assert(movedPlayerSnake.head.pos == Position(3, 2))
    })
  }

  describe("The server reconciliation") {
    it("should have no effect when the client state matches the server state")(new ClientGameScope {
      val numTickWhenToChangeMove = 4
      runClientGameWithOneMoveChange(numTickWhenToChangeMove, nextMove)

      val savedClientState = game.clientState
      game.onGameLoopNotif(GameLoopNotif(new GameLoopId(numTickWhenToChangeMove), snakeMoves = Set(SnakeMove(playerSnake.snakeId, nextMove))))
      assert(savedClientState == game.clientState)
    })


    it("should take place when the client state diverges from the server state")(new ClientGameScope {
      val numTickWhenToChangeMove = 4
      runClientGameWithOneMoveChange(numTickWhenToChangeMove, nextMove)

      val savedClientState = game.clientState
      val serverNotifId = new GameLoopId(2)
      val nbCorrectClientMoves = serverNotifId - 1
      game.onGameLoopNotif(GameLoopNotif(serverNotifId, snakeMoves = Set(SnakeMove(otherSnake.snakeId, Down))))

      val expectedOtherSnakePos = Position(otherSnake.head.pos.x + nbCorrectClientMoves, otherSnake.head.pos.y + NbTicks - nbCorrectClientMoves)
      assert(savedClientState != game.clientState)
      assert(getSnakeFromState(savedClientState, playerSnake.snakeId) == getSnakeFromState(game.clientState, playerSnake.snakeId))
      assert(getSnakeFromState(game.clientState, otherSnake.snakeId).get.head.pos == expectedOtherSnakePos)
    })

    it("should replace the client state by the server state when serverState.id >= clientState.id")(new ClientGameScope {
      val numTickWhenToChangeMove = 4
      runClientGameWithOneMoveChange(numTickWhenToChangeMove, nextMove)

      val savedClientState = game.clientState
      val serverNotifId = new GameLoopId(NbTicks + 2)
      game.onGameLoopNotif(GameLoopNotif(serverNotifId, snakeMoves = Set(SnakeMove(otherSnake.snakeId, Down))))

    })
  }

  def runClientGameWithOneMoveChange(numTickWhenToChangeMove: Int, nextMove: Move) {
    for (numTick <- 1 to NbTicks) {
      if (numTick == numTickWhenToChangeMove) {
        game.nextMove = nextMove

      }
      game.onGameTick
    }
  }
}
