package shared.models

import IdTypes.SnakeId
import IdTypes.GameLoopId
import Moves.Move
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import shared.models.GameNotifJsonImplicits._

case class SnakeMove(snakeId: SnakeId, move: Move)

sealed trait GameNotif {
  def notifType: String
  def toJson: JsValue
}

case class GameLoopNotif(gameLoopId: GameLoopId = new GameLoopId(0), foods: Set[Block] = Set(), snakeMoves: Set[SnakeMove] = Set(), deadSnakes: Set[SnakeId] = Set(), override val notifType: String = "gameLoop") extends GameNotif {

  def isEmpty: Boolean = foods.isEmpty && snakeMoves.isEmpty && deadSnakes.isEmpty

  def withNewSnakeMove(snakeMove: SnakeMove) = {
    val snakesMap: Map[SnakeId, Move] = snakeMoves.map(snake => (snake.snakeId, snake.move)).toMap + (snakeMove.snakeId -> snakeMove.move)
    val uniqueSnakeMoves = for ((id, move) <- snakesMap) yield {
      SnakeMove(id, move)
    }

    copy(snakeMoves = uniqueSnakeMoves.toSet)
  }

  def incGameLoopId: GameLoopNotif = copy(gameLoopId = new GameLoopId(gameLoopId.id + 1))

  def toJson = Json.toJson(this)
}

case class GameInitNotif(snakes: Seq[Snake], override val notifType: String = "gameInit") extends GameNotif {
  def toJson = Json.toJson(this)
}

case class PlayerSnakeIdNotif(playerSnakeId: SnakeId, override val notifType: String = "playerSnakeId") extends GameNotif {
  def toJson = Json.toJson(this)
}

case class DisconnectedSnakeNotif(disconnectedSnakeId: SnakeId, override val notifType: String = "disconnectedSnake") extends GameNotif {
  def toJson = Json.toJson(this)
}

case class Heartbeat(override val notifType: String = "heartbeat") extends GameNotif {
  def toJson = Json.toJson(this)
}
