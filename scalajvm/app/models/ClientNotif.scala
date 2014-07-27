package models

import shared.models.Moves.Move
import play.api.libs.json.Json
import shared.models.GameNotifJsonImplicits._

case class ClientNotif(move: Move)

object ClientNotif {
  implicit val clientFormat = Json.format[ClientNotif]
}

