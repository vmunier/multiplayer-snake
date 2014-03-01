package game
import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import scala.collection.mutable.HashMap

object Keyboard {
  private val keysDown = HashMap[js.Number, Boolean]()

  def init() = {
    g.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      keysDown += e.keyCode -> true
    }, false)

    g.addEventListener("keyup", (e: dom.KeyboardEvent) => {
      keysDown -= e.keyCode
    }, false)
  }


  def isHoldingLeft = keysDown.contains(37)
  def isHoldingUp = keysDown.contains(38)
  def isHoldingRight = keysDown.contains(39)
  def isHoldingDown = keysDown.contains(40)
}
