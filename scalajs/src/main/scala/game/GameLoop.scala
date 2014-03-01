package game

import scala.scalajs.js
import js.Dynamic.{ global => g }

class GameLoop {

  var then = js.Date.now()
  def loop(update: (js.Number) => Unit, render: () => Unit): () => Unit = () => {
    g.window.requestAnimationFrame(loop(update, render))

    val now = js.Date.now()
    val delta = now - then

    update(delta / 1000)
    render()

    then = now
  }

  def start(update: (js.Number) => Unit, render: () => Unit) = {
    loop(update, render)()
  }
}
