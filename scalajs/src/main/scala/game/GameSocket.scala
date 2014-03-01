package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.extensions._

import shared.models._
import shared.models.IdTypes.SnakeId
import shared.models.Moves._

object GameSocket {

  val receiveGameNotif = (notif: GameNotif) => {
    println("notif  : " + notif)
    println("foods : " + notif.snakes.toSeq.map(_.move))
  }

  def init() = {
    //println("in init!")
    g.window.receiveGameNotif = (x: JsGameNotif) => receiveGameNotif(GameNotifParser.parse(x))
    //g.window.receiveGameNotif = (x: GameNotifJs) => println(x)
  }
}
