package actors

import scala.concurrent.Promise
import akka.actor.Actor
import shared.models.Block
import shared.models.GameConstants
import shared.models.Moves._
import shared.models.Position
import shared.models.Snake
import shared.services.MoveService
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import shared.services.SnakeService
import shared.models.mutable
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue
import shared.models.GameNotif
import shared.models.Colors
import shared.services.BlockService
import shared.models.SnakeMove
import shared.models.IdTypes._

object GameActor {
  case class MoveSnake(snakeId: SnakeId, move: Move)
  case object DisposeNewFood
  case object GameTick
  case class Join(snakeIdPromise: Promise[SnakeId])
  case object Start
}

trait GameVars extends mutable.GameFood {
  var snakes = Map[SnakeId, Snake]()
  var losingSnakes = Map[SnakeId, Snake]()
  var nextGameNotif = GameNotif()
}

trait StartedGame {
  def started: Actor.Receive
}

trait GameConnections extends GameVars with GameConstants { actor: Actor with StartedGame  =>
  import GameActor._


  override def receive: Actor.Receive = {
    case Start =>
      onStart()
    case Join(snakeIdPromise) =>
      onJoin(snakeIdPromise)
  }

  def onStart() {
    context.become(started)
    Akka.system.scheduler.schedule(0.milliseconds, GameTickInterval) {
      self ! GameTick
    }
    Akka.system.scheduler.schedule(0.milliseconds, NewFoodInterval) {
      self ! DisposeNewFood
    }
  }

  def onJoin(snakeIdPromise: Promise[SnakeId]) = {
    val snakeId = new SnakeId(snakes.size)
    val availablePositions = blockPositions.diff(snakes.values.map(_.blocks).toSeq)
    val snakeHead = BlockService.randomNewBlock(availablePositions)
    snakes += snakeId -> Snake(snakeHead)
    snakeIdPromise.success(snakeId)
  }
}

class GameActor(notifsChannel: Channel[GameNotif]) extends Actor with StartedGame with GameConnections {
  import GameActor._

  def started: Actor.Receive = {
    case DisposeNewFood =>
      onDisposeNewFood()
    case GameTick =>
      onGameTick()
    case MoveSnake(snakeId, move) =>
      onMoveSnake(snakeId, move)
  }

  def onMoveSnake(snakeId: SnakeId, move: Move) {
    println("in onMoveSnake")
    for {
      snake <- snakes.get(snakeId)
      if MoveService.isValidMove(snake, move)
    } {
      println("update happens to move=" + move)
      nextGameNotif = nextGameNotif.withNewSnakeMove(SnakeMove(snakeId, move))
      snakes += snakeId -> snake.copy(move = move)
    }
  }

  def onGameTick() = {
    println("in onGameTick!")
    if (foods.isEmpty) {
      addNewFood(availablePositions)
    }

    for ((snakeId, snake) <- snakes) {
      moveSnake(snakeId, snake)
      handleCollisions(snakeId, snake)
    }

    notifsChannel.push(nextGameNotif)
    nextGameNotif = GameNotif()
  }

  private def moveSnake(snakeId: SnakeId, snake: Snake) {
    val movedSnake = SnakeService.moveSnake(snake, NbBlocksInWidth, NbBlocksInHeight)
    snakes += snakeId -> movedSnake
  }

  private def handleCollisions(snakeId: SnakeId, snake: Snake) = {
    if (snake.bitesItsQueue) {
      // gameOver for snake
      snakes -= snakeId
      losingSnakes += snakeId -> snake
    } else {
      // digested food
      for (eatenFood <- BlockService.findCollision(snake.head, foods)) {
        startDigestionForFood(eatenFood)
        snakes += snakeId -> snake.copy(blocksEaten = snake.blocksEaten + 1)
      }

      // food reaching queue
      for (foodReachingEndQueue <- BlockService.findCollision(snake.blocks.last, foodsInDigestion)) {
        snakes += snakeId -> snake.copy(tail = snake.tail ++ Seq(foodReachingEndQueue))
        endDigestionForFood(foodReachingEndQueue)
      }
    }
  }

  def onDisposeNewFood() = {
    if (foods.size < MaxFoodAtSameTime) {
      addNewFood(availablePositions)
    }
  }

  override def addNewFood(avlblePositions: IndexedSeq[Position]) = {
    val newFood = super.addNewFood(avlblePositions)
    nextGameNotif = nextGameNotif.copy(foods = nextGameNotif.foods + newFood)
    newFood
  }

  def availablePositions: IndexedSeq[Position] = {
    val reservedBlocks = snakes.values.flatMap(_.blocks).toSeq ++ foods ++ foodsInDigestion
    blockPositions.diff(reservedBlocks.map(_.pos))
  }
}
