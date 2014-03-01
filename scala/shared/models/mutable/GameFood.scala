package shared.models.mutable

import shared.models.Block
import shared.models.Position
import shared.services.BlockService

trait GameFood {
  var foods: Set[Block] = Set[Block]()
  var foodsInDigestion: Set[Block] = Set[Block]()

  def startDigestionForFood(food: Block) = {
    foods -= food
    foodsInDigestion += food
  }

  def endDigestionForFood(food: Block) = {
    foodsInDigestion -= food
  }

  def addNewFood(availablePositions: IndexedSeq[Position]): Block = {
    val randomBlock = BlockService.randomNewBlock(availablePositions)
    foods += randomBlock
    randomBlock
  }
}

