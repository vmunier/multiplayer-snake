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
import shared.models.GameNotif

case class Game(gameId: GameId) {
  import GameActor._

  private val (enumerator, channel) = Concurrent.broadcast[GameNotif]
  private val gameActorRef = Akka.system.actorOf(Props(new GameActor(channel)))

  val notifsEnumerator: Enumerator[GameNotif] = enumerator

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

