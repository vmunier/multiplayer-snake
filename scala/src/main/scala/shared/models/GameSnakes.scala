package shared.models

import shared.models.IdTypes.SnakeId

case class GameSnakes(alive: Seq[Snake] = Seq(), dead: Seq[Snake] = Seq()) {
  lazy val aliveMap = toMap(alive)
  lazy val deadMap = toMap(dead)

  lazy val all = alive ++ dead
  lazy val allMap = toMap(all)

  assert(all.groupBy(_.snakeId).values.forall(_.size <= 1))
  private def toMap(snakes: Seq[Snake]): Map[SnakeId, Snake] = {
    snakes.map(s => s.snakeId -> s).toMap
  }

  def addAliveSnakes(aliveSnakes: Seq[Snake]): GameSnakes = {
    copy(alive = aliveSnakes ++ alive)
  }

  def addDeadSnakes(deadSnakes: Seq[Snake]): GameSnakes = {
    GameSnakes((aliveMap -- deadSnakes.map(_.snakeId)).values.toSeq, dead ++ deadSnakes)
  }

  def addDeadSnakeIds(deadSnakeIds: SnakeId*): GameSnakes = {
    addDeadSnakes(deadSnakeIds.flatMap(aliveMap.get(_)))
  }

  def mergeAliveSnakes(otherAliveSnakes: Seq[Snake]): GameSnakes = {
    val othersMap = otherAliveSnakes.map(s => s.snakeId -> s).toMap
    val mergedSnakes = (aliveMap ++ othersMap).values.toSeq
    copy(alive = mergedSnakes)
  }
}
