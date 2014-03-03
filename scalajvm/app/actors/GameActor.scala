package actors

import scala.concurrent.Promise
import akka.actor.Actor
import shared.models.Block
import shared.models.GameConstants
import shared.models.Moves._
import shared.models.Position
import shared.models.DisconnectedSnakeNotif
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
import shared.models.GameLoopNotif
import shared.models.GameInitNotif
import shared.models.IdTypes._
import play.api.libs.json.Json
import models.GameNotifJsonImplicits._
import akka.actor.Cancellable

object GameActor {
  case class MoveSnake(snakeId: SnakeId, move: Move)
  case class DisconnectSnake(snakeId: SnakeId)
  case object DisposeNewFood
  case object GameTick
  case class Join(snakeIdPromise: Promise[SnakeId])
  case class Start(startedPromise: Promise[Boolean])
}

trait StartedGame {
  def started: Actor.Receive
  def notifsChannel: Channel[JsValue]
}

trait GameConnections extends mutable.GameMutations { actor: Actor with StartedGame =>
  import GameActor._

  override def receive: Actor.Receive = {
    case Start(startedPromise) =>
      onStart(startedPromise)
    case Join(snakeIdPromise) =>
      onJoin(snakeIdPromise)
    case DisconnectSnake(snakeId) =>
      onDisconnectSnake(snakeId)
  }

  val gameTickScheduler = Promise[Cancellable]
  val newFoodScheduler = Promise[Cancellable]

  def onStart(startedPromise: Promise[Boolean]) {
    if (snakes.size <= 1) {
      startedPromise.success(false)
    } else {
      context.become(started)
      gameTickScheduler.success {
        Akka.system.scheduler.schedule(0.milliseconds, GameTickInterval) {
          self ! GameTick
        }
      }
      newFoodScheduler.success {
        Akka.system.scheduler.schedule(0.milliseconds, NewFoodInterval) {
          self ! DisposeNewFood
        }
      }

      val gameInitNotif = GameInitNotif(snakes.values.toSeq)
      notifsChannel.push(Json.toJson(gameInitNotif))
      startedPromise.success(true)
    }
  }

  var nextSnakeId = 0
  def onJoin(snakeIdPromise: Promise[SnakeId]) {
    val snakeId = new SnakeId(nextSnakeId)
    nextSnakeId += 1
    val availablePositions = blockPositions.diff(snakes.values.map(_.blocks).toSeq)
    val snakeHead = BlockService.randomNewBlock(availablePositions)
    snakes += snakeId -> Snake(snakeId, snakeHead)
    snakeIdPromise.success(snakeId)
  }

  def onDisconnectSnake(snakeId: SnakeId) {
    notifsChannel.push(Json.toJson(DisconnectedSnakeNotif(snakeId)))
    killSnake(snakeId)
  }
}

class GameActor(override val notifsChannel: Channel[JsValue]) extends Actor with StartedGame with GameConnections {
  import GameActor._

  var nextGameNotif = GameLoopNotif()

  def started: Actor.Receive = {
    case DisposeNewFood =>
      onDisposeNewFood()
    case GameTick =>
      onGameTick()
    case MoveSnake(snakeId, move) =>
      onMoveSnake(snakeId, move)
    case DisconnectSnake(snakeId) =>
      onDisconnectSnake(snakeId)
  }

  def onMoveSnake(snakeId: SnakeId, move: Move) {
    for {
      snake <- snakes.get(snakeId)
      if MoveService.isValidMove(snake, move)
    } {
      nextGameNotif = nextGameNotif.withNewSnakeMove(SnakeMove(snakeId, move))
    }
  }

  def onGameTick() = {
    for {
      SnakeMove(snakeId, move) <- nextGameNotif.snakes
      snake <- snakes.get(snakeId)
    } {
      snakes += snakeId -> snake.copy(move = move)
    }
    super.moveSnakes()

    if (foods.isEmpty) {
      addNewFood(availablePositions)
    }
    notifsChannel.push(Json.toJson(nextGameNotif))
    nextGameNotif = GameLoopNotif()

    if (snakes.size <= 1) {
      stopAll()
    }
  }

  private def stopAll() = {
    context.stop(self)
    for (schedulerPromise <- Seq(gameTickScheduler, newFoodScheduler)) {
      schedulerPromise.future.foreach(_.cancel())
    }
    notifsChannel.eofAndEnd()
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
