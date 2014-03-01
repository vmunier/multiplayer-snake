package game
import scala.scalajs.js
import js.Dynamic.{ global => g }
import shared.models.GameNotif
import shared.models.Block
import shared.models.Position
import shared.models.Moves
import shared.models.SnakeMove
import shared.models.IdTypes.SnakeId

object GameNotifParser {
  def parse(notifJs: JsGameNotif): GameNotif = {
    val jsFoods = notifJs.foods.toSet
    val foods = for (food <- jsFoods) yield {
      Block(Position(food.pos.x, food.pos.y), food.style)
    }

    val jsSnakes = notifJs.snakes.toSet

    val snakes = for {
      snake <- jsSnakes
      move <- Moves.fromName.lift(snake.move)
    } yield {
      SnakeMove(new SnakeId(snake.snakeId), move)
    }
    GameNotif(foods, snakes)
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

trait JsGameNotif extends js.Object {
  def foods: js.Array[JsBlock]
  def snakes: js.Array[JsSnakeMove]
}
