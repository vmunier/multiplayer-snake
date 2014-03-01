package models

import scala.concurrent.Future
import scala.concurrent.Promise
import actors.GameActor
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import shared.models.Snake
import shared.models.Moves._
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Enumerator
import shared.models.IdTypes._
import shared.models.GameLoopNotif
import shared.models.GameInitNotif
import java.util.UUID
import play.api.libs.json.JsValue

case class Game(gameId: GameId, creatorUUID: UUID) {
  import GameActor._

  private val (enumerator, channel) = Concurrent.broadcast[JsValue]
  private val gameActorRef = Akka.system.actorOf(Props(new GameActor(channel)))

  val notifsEnumerator: Enumerator[JsValue] = enumerator

  def join(): Future[SnakeId] = {
    val snakeIdPromise = Promise[SnakeId]
    gameActorRef ! Join(snakeIdPromise)
    snakeIdPromise.future
  }

  def start(): Unit = {
    gameActorRef ! Start
  }

  def moveSnake(snakeId: SnakeId, move: Move): Unit = {
    gameActorRef ! MoveSnake(snakeId, move)
  }
}

