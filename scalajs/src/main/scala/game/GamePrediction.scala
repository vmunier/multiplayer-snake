package game

import shared.models.mutable
import scala.scalajs.js
import js.Dynamic.{ global => g }
import shared.models.GameConstants
import shared.models.GameConstants._

trait GamePrediction {

  def callOnTick(): Unit
  private var intervalId: Int = _

  def startGamePrediction() = {
    g.setInterval(callOnTick, GameTickInterval.toMillis)
  }

  def stopGamePrediction() = {
    g.clearInterval(intervalId)
  }

  def restartGamePrediction() = {
    stopGamePrediction()
    startGamePrediction()
  }
}