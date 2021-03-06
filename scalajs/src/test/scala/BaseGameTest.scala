package tests

import models._
import shared.models.Position
import shared.models.Snake
import shared.models.Block
import shared.models.GameState
import shared.models.GameSnakes
import shared.models.GameLoopNotif
import shared.models.Moves._
import shared.models.IdTypes._
import scala.scalajs.test.JasmineTest

abstract class BaseGameTest extends JasmineTest {
  val game = new TestableGame()

  val playerSnake = Snake(new SnakeId(0), Block(Position(2,2), ""))
  val otherSnake = Snake(new SnakeId(1), Block(Position(2,3), ""))

  val firstLoopState = GameLoopState(GameState(GameSnakes(Seq(playerSnake, otherSnake))), new GameLoopId(0))


  beforeEach {
    game.nextMove = Right
    game.serverState = GameLoopState()
    game.clientState = GameLoopState()
    game.playerSnakeId = new SnakeId(0)
  }

  def getSnakeFromState(gameLoopState: GameLoopState, snakeId: SnakeId): Option[Snake] = {
    gameLoopState.gameState.snakes.aliveMap.get(snakeId)
  }
}
