package shared

trait GameConstants {
  val NbBlocksInHeigth = 25
  val NbBlocksInWidth = (NbBlocksInHeigth * 1.5).toInt

  val FoodPeriodApparition = 15
  val MaxFoodAtSameTime = 3

  val blockPositions =
    for {
      x <- 0 until NbBlocksInWidth
      y <- 0 until NbBlocksInHeigth
    } yield {
      Position(x, y)
    }
}
