package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import scala.collection.mutable.HashSet

class Keyboard(gameSocket: WebSocket) {

  private val keysDown = HashSet[js.Number]()
  private val keyCodeToMove: Map[Int, String] = Map(
    37 -> "left",
    38 -> "up",
    39 -> "right",
    40 -> "down")

  def registerEventListeners() = {
    g.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      for (move <- keyCodeToMove.get(e.keyCode.toInt)) {
        sendMove(move)
      }
      keysDown += e.keyCode
    }, false)

    g.addEventListener("keyup", (e: dom.KeyboardEvent) => {
      keysDown -= e.keyCode
    }, false)
  }

  private def sendMove(move: String): Unit = {
    gameSocket.send(s"""{"move": "$move"}""");
  }

  def isHoldingLeft = keysDown.contains(37)
  def isHoldingUp = keysDown.contains(38)
  def isHoldingRight = keysDown.contains(39)
  def isHoldingDown = keysDown.contains(40)
}