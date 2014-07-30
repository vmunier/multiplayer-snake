package jsfacades

import org.scalajs.dom

import scala.scalajs.js

class HTMLImageElement extends dom.HTMLImageElement {
  var onload: js.Function1[dom.Event, _] = ???
}
