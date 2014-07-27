package models

import java.util.UUID

import actors.GameActor
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.{Concurrent, Enumerator}
import shared.models.GameNotif
import shared.models.IdTypes._
import shared.models.Moves._

import scala.concurrent.{Future, Promise}

case class Game(gameId: GameId, creatorUUID: UUID, name: String) {
  import actors.GameActor._

  private val (enumerator, channel) = Concurrent.broadcast[GameNotif]
  private val gameActorRef = Akka.system.actorOf(Props(new GameActor(channel)))

  val notifsEnumerator: Enumerator[GameNotif] = enumerator

  def disconnectSnake(snakeId: SnakeId): Unit = {
    gameActorRef ! DisconnectSnake(snakeId)
  }

  def join(): Future[SnakeId] = {
    val snakeIdPromise = Promise[SnakeId]
    gameActorRef ! Join(snakeIdPromise)
    snakeIdPromise.future
  }

  def start(): Future[Boolean] = {
    val startedPromise = Promise[Boolean]
    gameActorRef ! Start(startedPromise)
    startedPromise.future
  }

  def stopIfNotStarted(): Unit = {
    gameActorRef ! Stop
  }

  def moveSnake(snakeId: SnakeId, move: Move): Unit = {
    gameActorRef ! MoveSnake(snakeId, move)
  }
}

