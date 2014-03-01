package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.extensions._
import org.scalajs.dom.HTMLCanvasElement
import shared.Snake
import shared.Block
import shared.Position
import shared.Colors
import shared.Moves._
import shared.GameConstants

trait GameVars extends GameConstants {

  lazy val BlockSize = Math.min(
  (Canvas.windowHeight / NbBlocksInHeigth).toInt,
  (Canvas.windowWidth / NbBlocksInWidth).toInt)

  val canvas = Canvas.init()

  var snake = Snake(
    head = Block(Position(0, 0), Colors.nextColor.rgb))

  var moveDoneInLastSecond = false
  var gameOver = false
  var win = false
}

object Game extends GameVars {
  Keyboard.init()

  def updateMove(): Unit = {
    val noTail = snake.tail.isEmpty
    val prevMove = snake.move
    val newMove =
      if (Keyboard.isHoldingLeft && (snake.move != Right || noTail)) Left
      else if (Keyboard.isHoldingRight && (snake.move != Left || noTail)) Right
      else if (Keyboard.isHoldingUp && (snake.move != Down || noTail)) Up
      else if (Keyboard.isHoldingDown && (snake.move != Up || noTail)) Down
      else snake.move

    if (prevMove != newMove) {
      snake = snake.copy(move = newMove)
      moveDoneInLastSecond = true
    }
  }

  var secondsAcc: js.Number = 0

  def update(seconds: js.Number) = {
    if (gameOver) {
      // do nothing
    } else if (snake.blocksEaten >= (blockPositions.size / 2)) {
      //} else if (Food.availablePositions.isEmpty) {
      gameOver = true
      win = true
    } else {
      println("moveDoneInLastSecond : ", moveDoneInLastSecond)
      if (!moveDoneInLastSecond) {
        updateMove()
      }
      secondsAcc += seconds
      if (secondsAcc * snake.speed >= 1) {
        moveDoneInLastSecond = false
        disposeNewFood()
        moveSnake()
        handleCollisions()
        secondsAcc = 0
      }
    }
  }

  private def handleCollisions() = {
    val snakeBitesItsQueue = snake.tail.map(_.pos).contains(snake.head.pos)

    if (snakeBitesItsQueue) {
      gameOver = true
    } else {
      Food.foods.filter(food => food.pos == snake.head.pos).headOption.map { eatenFood =>
        Food.startDigestionForFood(eatenFood)
        snake = snake.copy(blocksEaten = snake.blocksEaten + 1)
      }

      Food.foodsInDigestion.filter(food => food.pos == snake.blocks.last.pos).headOption.map { foodReachingEndQueue =>
        snake = snake.copy(tail = snake.tail ++ Seq(foodReachingEndQueue))
        Food.endDigestionForFood(foodReachingEndQueue)
      }
    }
  }

  private def disposeNewFood() = {
    if (Food.foods.isEmpty ||
      (Food.foods.size < MaxFoodAtSameTime && js.Math.abs(Math.random() * FoodPeriodApparition).toInt == 0)) {
      Food.addRandomFood()
    }
  }

  def moveSnake() = {
    val modif = BlockSize
    snake = snake.copy(tail = snake.moveTailForward())

    val headPos = snake.head.pos

    val newHeadPos = Position(
      x = (NbBlocksInWidth + headPos.x + horizontal) % NbBlocksInWidth,
      y = (NbBlocksInHeigth + headPos.y + vertical) % NbBlocksInHeigth)

    snake = snake.copy(head = snake.head.copy(pos = newHeadPos))
  }

  def horizontal() = snake.move match {
    case Left => -1
    case Right => 1
    case _ => 0
  }

  def vertical() = snake.move match {
    case Up => -1
    case Down => 1
    case _ => 0
  }

  def main(): Unit = {
    new GameLoop().start(update, () => Canvas.render(snake, Food.foods.toSeq, Food.foodsInDigestion.toSeq))
  }
}
