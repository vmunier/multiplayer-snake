package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import shared.models.Moves._

class Keyboard() {
  private val keyCodeToMove: Map[Int, Move] = Map(
    37 -> Left,
    38 -> Up,
    39 -> Right,
    40 -> Down)
  private val listeners = ArrayBuffer[Move => Unit]()

  def registerEventListeners() = {
    g.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      for (move <- keyCodeToMove.get(e.keyCode.toInt)) {
        notifyListeners(move)
      }
    }, false)
  }

  def onMove(listener: Move => Unit) = {
    listeners += listener
  }

  private def notifyListeners(move: Move) = {
    for (listener <- listeners) {
      listener(move)
    }
  }
}