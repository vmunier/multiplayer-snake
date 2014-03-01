package game

import shared.models.Block
import shared.models.Position
import shared.models.Colors

object Food {
  var foods = Set[Block]()
  var foodsInDigestion = Set[Block]()

  def addRandomFood(): Block = {
    val randomPos = availablePositions((Math.random() * availablePositions.size).toInt)
    val randomBlock = Block(randomPos, Colors.nextColor.rgb)
    foods += randomBlock
    randomBlock
  }

  def availablePositions: Seq[Position] = {
    val reservedBlocks = Game.snake.blocks ++ foods ++ foodsInDigestion
    Game.blockPositions.diff(reservedBlocks.map(_.pos))
  }

  def startDigestionForFood(food: Block) = {
    foods -= food
    foodsInDigestion += food
  }

  def endDigestionForFood(food: Block) = {
    foodsInDigestion -= food
  }
}
