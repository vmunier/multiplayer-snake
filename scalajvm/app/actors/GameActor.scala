package actors

import shared.services.{TurnService, GameStateService, MoveService, BlockService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.Cancellable
import akka.actor.actorRef2Scala
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Concurrent.Channel
import shared.models.GameConstants._
import shared.models.IdTypes._
import shared.models.Moves.Move
import shared.models.GameNotif
import shared.models.Position
import shared.models.SnakeMove
import shared.models.GameState
import shared.models.DisconnectedSnakeNotif
import shared.models.Snake
import shared.models.Heartbeat
import shared.models.GameLoopNotif
import shared.models.GameInitNotif

object GameActor {
  case class MoveSnake(snakeId: SnakeId, move: Move)
  case class DisconnectSnake(snakeId: SnakeId)
  case object DisposeNewFood
  case object GameTick
  case object ConnectionsHeartbeat
  case class Join(snakeIdPromise: Promise[SnakeId])
  case class Start(startedPromise: Promise[Boolean])
  case object Stop
}

trait StartedGame {
  def started: Actor.Receive
  def notifsChannel: Channel[GameNotif]
}

trait GameConnections { actor: Actor with StartedGame =>
  import GameActor._

  var gameState = GameState()

  val heartbeatScheduler = Akka.system.scheduler.schedule(0.milliseconds, HeartbeatInterval) {
    notifsChannel.push(Heartbeat())
  }

  override def receive: Actor.Receive = {
    case Start(startedPromise) =>
      onStart(startedPromise)
    case Stop =>
      onStop()
    case Join(snakeIdPromise) =>
      onJoin(snakeIdPromise)
    case DisconnectSnake(snakeId) =>
      onDisconnectSnake(snakeId)
  }

  val gameTickScheduler = Promise[Cancellable]
  val newFoodScheduler = Promise[Cancellable]

  def onStart(startedPromise: Promise[Boolean]) {
    if (gameState.snakes.all.size <= 1) {
      startedPromise.success(false)
    } else {
      context.become(started)
      heartbeatScheduler.cancel()
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

      val gameInitNotif = GameInitNotif(gameState.snakes.all)
      notifsChannel.push(gameInitNotif)
      startedPromise.success(true)
    }
  }

  def onStop() {
    heartbeatScheduler.cancel()
    notifsChannel.eofAndEnd()
  }

  var nextSnakeId = 0
  def onJoin(snakeIdPromise: Promise[SnakeId]) {
    val snakeId = new SnakeId(nextSnakeId)
    nextSnakeId += 1
    val availablePositions = blockPositions.diff(gameState.snakes.alive.map(_.blocks).toSeq)
    val snakeHead = BlockService.randomNewBlock(availablePositions)
    gameState = gameState.copy(snakes = gameState.snakes.addAliveSnakes(Seq(Snake(snakeId, snakeHead))))
    snakeIdPromise.success(snakeId)
  }

  def onDisconnectSnake(snakeId: SnakeId) {
    notifsChannel.push(DisconnectedSnakeNotif(snakeId))
    gameState = gameState.copy(snakes = gameState.snakes.addDeadSnakeIds(snakeId))
  }
}

class GameActor(override val notifsChannel: Channel[GameNotif]) extends Actor with StartedGame with GameConnections {
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
      snake <- gameState.snakes.aliveMap.get(snakeId)
      if MoveService.isValidMove(snake, move)
    } {
      nextGameNotif = nextGameNotif.withNewSnakeMove(SnakeMove(snakeId, move))
    }
  }

  def onGameTick() {
    val newGameState = (GameStateService.changeSnakeMoves(nextGameNotif.snakeMoves) _ andThen TurnService.afterTurn _)(gameState)

    addDeadSnakesToNotif(gameState, newGameState)

    gameState = newGameState

    if (gameState.foods.available.isEmpty) {
     addNewFood(availablePositions)
    }

    nextGameNotif = nextGameNotif.incGameLoopId

    if (!nextGameNotif.isEmpty || PushForced.isPushForced(nextGameNotif.gameLoopId)) {
      notifsChannel.push(nextGameNotif)
    }
    nextGameNotif = new GameLoopNotif(nextGameNotif.gameLoopId)

    if (gameState.snakes.alive.size <= 1) {
      stopAll()
    }
  }

  private def addDeadSnakesToNotif(prevGameState: GameState, newGameState: GameState) = {
   nextGameNotif = nextGameNotif.copy(deadSnakes = prevGameState.snakes.alive.map(_.snakeId).diff(newGameState.snakes.alive.map(_.snakeId)).toSet)
  }

  private def stopAll() = {
    for (schedulerPromise <- Seq(gameTickScheduler, newFoodScheduler)) {
      schedulerPromise.future.foreach(_.cancel())
    }
    notifsChannel.eofAndEnd()
    context.stop(self)
  }

  def onDisposeNewFood() = {
    if (gameState.foods.available.size < MaxFoodAtSameTime) {
      addNewFood(availablePositions)
    }
  }

  def addNewFood(avlblePositions: IndexedSeq[Position]) = {
    val newFood = BlockService.randomNewBlock(availablePositions)

    gameState = GameStateService.addNewFood(newFood)(gameState)

    nextGameNotif = nextGameNotif.copy(foods = nextGameNotif.foods + newFood)
    newFood
  }

  def availablePositions: IndexedSeq[Position] = {
    val reservedBlocks = gameState.snakes.alive.flatMap(_.blocks).toSeq ++ gameState.foods.all
    blockPositions.diff(reservedBlocks.map(_.pos))
  }
}

object PushForced {
  var lastPushGameLoopId = 0

  def isPushForced(gameLoopId: GameLoopId): Boolean = {
    val pushForced = (gameLoopId - lastPushGameLoopId) % PushForceEveryNbGameLoops == 0
    if (pushForced) {
      lastPushGameLoopId = gameLoopId
    }
    pushForced
  }
}
