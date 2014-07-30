package shared.models
import scala.concurrent.duration._

object GameConstants {
  val NbBlocksInHeight = 25
  val NbBlocksInWidth = (NbBlocksInHeight * 1.5).toInt

  val FoodPeriodApparition = 15
  val MaxFoodAtSameTime = 3

  val GameTickInterval: FiniteDuration = 100.milliseconds
  val NewFoodInterval: FiniteDuration = 3.seconds
  val HeartbeatInterval: FiniteDuration = 1.seconds
  val WaitDurationBeforeRemovingGameIfNotStarted = 10.minutes

  val PushForceEveryNbGameLoops = 20

  val blockPositions =
    for {
      x <- 0 until NbBlocksInWidth
      y <- 0 until NbBlocksInHeight
    } yield {
      Position(x, y)
    }
}
