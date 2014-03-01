package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.extensions._

import shared.models._
import shared.models.IdTypes.SnakeId
import shared.models.Moves._

case class GameLoop(update: (GameNotif) => Unit, render: () => Unit) {
  val receiveGameNotif = (gameNotif: GameNotif) => {
    println("notif  : " + gameNotif)
    println("foods : " + gameNotif.snakes.toSeq.map(_.move))
    update(gameNotif)
    render()
  }

  def start() = {
    g.window.game.receiveGameNotif = (x: JsGameNotif) => receiveGameNotif(GameNotifParser.parse(x))
  }
}
