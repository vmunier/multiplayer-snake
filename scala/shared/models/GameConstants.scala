package shared.models
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

trait GameConstants {
  val NbBlocksInHeight = 25
  val NbBlocksInWidth = (NbBlocksInHeight * 1.5).toInt

  val FoodPeriodApparition = 15
  val MaxFoodAtSameTime = 3

  // real GameTickInterval
  val GameTickInterval: FiniteDuration = 100.milliseconds
  val NewFoodInterval: FiniteDuration = 3.seconds
  val HeartbeatInterval: FiniteDuration = 1.seconds

  val blockPositions =
    for {
      x <- 0 until NbBlocksInWidth
      y <- 0 until NbBlocksInHeight
    } yield {
      Position(x, y)
    }
}
