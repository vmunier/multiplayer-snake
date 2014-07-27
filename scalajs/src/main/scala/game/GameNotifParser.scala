package game
import scala.scalajs.js
import js.Dynamic.{ global => g }
import shared.models.GameNotif
import shared.models.Block
import shared.models.Position
import shared.models.Moves
import shared.models.SnakeMove
import shared.models.IdTypes.SnakeId
import shared.models.IdTypes.GameLoopId
import shared.models.GameLoopNotif
import shared.models.GameInitNotif
import shared.models.Snake

object GameNotifParser {
  def parseGameLoopNotif(jsGameLoopNotif: JsGameLoopNotif): GameLoopNotif = {
    val jsFoods = jsGameLoopNotif.foods.toSet
    val foods = for (food <- jsFoods) yield {
      parseBlock(food)
    }

    val jsSnakeMoves = jsGameLoopNotif.snakeMoves.toSet
    val snakeMoves = for {
      snakeMove <- jsSnakeMoves
      move <- Moves.fromName.lift(snakeMove.move)
    } yield {
      SnakeMove(new SnakeId(snakeMove.snakeId), move)
    }

    val jsDeadSnakes = jsGameLoopNotif.deadSnakes.toSet
    val deadSnakes = for (deadSnake <- jsDeadSnakes) yield {
      new SnakeId(deadSnake.toInt)
    }

    GameLoopNotif(new GameLoopId(jsGameLoopNotif.gameLoopId), foods, snakeMoves, deadSnakes)
  }

  def parseGameInitNotif(jsInitNotif: JsGameInitNotif): GameInitNotif = {
    val snakes = for {
      jsSnake <- jsInitNotif.snakes.toSeq
      move <- Moves.fromName.lift(jsSnake.move)
    } yield {
      val head = parseBlock(jsSnake.head)
      val tail = jsSnake.tail.toList.map(parseBlock(_))
      val snakeId = new SnakeId(jsSnake.snakeId)
      Snake(snakeId, head, tail, jsSnake.nbEatenBlocks, move)
    }

    GameInitNotif(snakes)
  }

  private def parseBlock(blockJs: JsBlock): Block = {
    Block(Position(blockJs.pos.x, blockJs.pos.y), blockJs.style)
  }
}

trait JsPosition extends js.Object {
  def x: Int
  def y: Int
}

trait JsBlock extends js.Object {
  def pos: JsPosition
  def style: String
}

trait JsSnakeMove extends js.Object {
  def snakeId: Int
  def move: String
}

trait JsGameLoopNotif extends js.Object {
  def gameLoopId: Int
  def notifType: String
  def foods: js.Array[JsBlock]
  def snakeMoves: js.Array[JsSnakeMove]
  def deadSnakes: js.Array[js.Number]
}

trait JsSnake extends js.Object {
  def snakeId: Int
  def head: JsBlock
  def tail: js.Array[JsBlock]
  def nbEatenBlocks: Int
  def move: String
}

trait JsGameInitNotif extends js.Object {
  def notifType: String
  def snakes: js.Array[JsSnake]
}

trait JsPlayerSnakeIdNotif extends js.Object {
  def notifType: String
  def playerSnakeId: Int
}

trait JsDisconnectedSnakeNotif extends js.Object {
  def notifType: String
  def disconnectedSnakeId: Int
}
