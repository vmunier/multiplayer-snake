package shared.models

case class GameFoods(available: Set[Block] = Set[Block](), inDigestion: Set[Block] = Set[Block]()) {
  lazy val all = available ++ inDigestion

  def startDigestionForFood(food: Block): GameFoods = {
    copy(available - food, inDigestion + food)
  }

  def endDigestionForFood(food: Block) = {
    copy(inDigestion = inDigestion - food)
  }
}