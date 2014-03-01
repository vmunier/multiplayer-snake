package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.extensions._

import shared.models._
import shared.models.IdTypes.SnakeId
import shared.models.Moves._

case class GameLoop(onGameLoopNotif: (GameLoopNotif) => Unit, render: () => Unit) {
  val receiveGameLoopNotif = (gameLoopNotif: GameLoopNotif) => {
    onGameLoopNotif(gameLoopNotif)
    render()
  }

  def start() = {
    g.window.game.receiveGameLoopNotif = (x: JsGameLoopNotif) => receiveGameLoopNotif(GameNotifParser.parseGameLoopNotif(x))
  }
}
