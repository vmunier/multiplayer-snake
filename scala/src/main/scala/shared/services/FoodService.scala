package shared.services

import shared.models.{GameFoods, Snake}

object FoodService {
  def eatAndDigested = Function.untupled(
    ((eatFoods _).tupled andThen (foodsReachingQueue _).tupled))

  def eatFoods(aliveSnakes: Seq[Snake], foods: GameFoods): (Seq[Snake], GameFoods) = foldSnakesWithFoods(aliveSnakes, foods) {
    (snake, updatedFoods) =>
      for (eatenFood <- BlockService.findCollision(snake.head, updatedFoods.available)) yield {
        (snake.copy(nbEatenBlocks = snake.nbEatenBlocks + 1),
          updatedFoods.startDigestionForFood(eatenFood))
      }
  }

  def foodsReachingQueue(aliveSnakes: Seq[Snake], foods: GameFoods): (Seq[Snake], GameFoods) = foldSnakesWithFoods(aliveSnakes, foods) {
    (snake, updatedFoods) =>
      for (foodReachingEndQueue <- BlockService.findCollision(snake.blocks.last, updatedFoods.inDigestion)) yield {
        (snake.copy(tail = snake.tail ++ Seq(foodReachingEndQueue)),
          updatedFoods.endDigestionForFood(foodReachingEndQueue))
      }
  }

  private type FoldFunc = (Snake, GameFoods) => Option[(Snake, GameFoods)]
  private def foldSnakesWithFoods(aliveSnakes: Seq[Snake], foods: GameFoods)(func: FoldFunc): (Seq[Snake], GameFoods) = {
    aliveSnakes.foldLeft((Seq[Snake](), foods)) {
      case ((updatedSnakes, updatedFoods), snake) =>
        func(snake, updatedFoods) match {
          case Some((newSnake, newFoods)) => (newSnake +: updatedSnakes, newFoods)
          case None => (snake +: updatedSnakes, updatedFoods)
        }
    }
  }
}