package shared.models

import play.api.libs.json.{JsError, JsResult, JsSuccess, JsValue, Json, Reads, Writes}
import shared.models.IdTypes.{GameLoopId, SnakeId}
import shared.models.Moves.{Move, moves}

object GameNotifJsonImplicits {

  // JSON implicits
  implicit val moveWrites = new Writes[Move] {
    def writes(move: Move): JsValue = Json.toJson(move.name)
  }

  implicit val moveReads = new Reads[Move] {
    override def reads(move: JsValue): JsResult[Move] = move.asOpt[String].collect { Moves.fromName }.map(JsSuccess(_))
      .getOrElse(JsError(s"Invalid move name (pick one between ${moves.map(_.name).mkString(", ")})"))
  }

  implicit val snakeIdWrites = new Writes[SnakeId] {
    def writes(snakeId: SnakeId): JsValue = Json.toJson(snakeId.id)
  }

  implicit val snakeIdReads = new Reads[SnakeId] {
    override def reads(snakeId: JsValue): JsResult[SnakeId] = snakeId.asOpt[Int].map(id => JsSuccess(new SnakeId(id)))
      .getOrElse(JsError(s"A SnakeId should be an integer"))
  }

  implicit val gameLoopIdWrites = new Writes[GameLoopId] {
    def writes(gameLoopId: GameLoopId): JsValue = Json.toJson(gameLoopId.id)
  }

  implicit val gameLoopIdReads = new Reads[GameLoopId] {
    override def reads(gameLoopId: JsValue): JsResult[GameLoopId] = gameLoopId.asOpt[Int].map(id => JsSuccess(new GameLoopId(id)))
      .getOrElse(JsError(s"A GameLoopId should be an integer"))
  }

  implicit val positionFormat = Json.format[Position]
  implicit val blockFormat = Json.format[Block]
  implicit val snakeMoveFormat = Json.format[SnakeMove]
  implicit val gameLoopNotifFormat = Json.format[GameLoopNotif]
  implicit val snakeFormat = Json.format[Snake]

  implicit val gameInitNotifFormat = Json.format[GameInitNotif]

  implicit val playerSnakeIdNotif = Json.format[PlayerSnakeIdNotif]
  implicit val disconnectedSnakeNotif = Json.format[DisconnectedSnakeNotif]

  implicit val heartbeatFormat = Json.format[Heartbeat]
}