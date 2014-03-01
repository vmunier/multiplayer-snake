package jsfacades

import scala.scalajs.js
import org.scalajs.dom

class HTMLImageElement extends dom.HTMLImageElement {
  var onload: js.Function1[dom.Event, _] = ???
}
