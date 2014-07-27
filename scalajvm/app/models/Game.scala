package models

import scala.concurrent.Future
import scala.concurrent.Promise
import actors.GameActor
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import shared.models.Moves._
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Enumerator
import shared.models.IdTypes._
import java.util.UUID
import play.api.libs.json.JsValue
import shared.models.GameNotif

case class Game(gameId: GameId, creatorUUID: UUID, name: String) {
  import GameActor._

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

