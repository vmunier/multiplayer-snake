package game

import scala.scalajs.js
import js.Dynamic.{ global => g }

class GameLoop {

  var prev = js.Date.now()
  def loop(update: (js.Number) => Unit, render: () => Unit): () => Unit = () => {
    g.window.requestAnimationFrame(loop(update, render))

    val now = js.Date.now()
    val delta = now - prev

    update(delta / 1000)
    render()

    prev = now
  }

  def start(update: (js.Number) => Unit, render: () => Unit) = {
    loop(update, render)()
  }
}
