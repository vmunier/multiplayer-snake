package actors
import java.util.UUID

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.Promise

import akka.actor.Actor
import akka.actor.Props
import exceptions.SnakeGameException
import models.Game
import shared.models.IdTypes.GameId
import shared.models.IdTypes.SnakeId
import shared.models.GameConstants.WaitDurationBeforeRemovingGameIfNotStarted
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.ExecutionContext.Implicits.global

object ConnectionsActor {
  case class CreateGame(gameName: String, gamePromise: Promise[Game])
  case class GetGame(gameId: GameId, gamePromise: Promise[Option[Game]])
  case class GetAllGames(gamesPromise: Promise[Seq[Game]])
  case class RemoveGame(gameId: GameId)

  val connectionsActor = Akka.system.actorOf(Props[ConnectionsActor])

  def createGame(gameName: String): Future[Game] = {
    val gamePromise = Promise[Game]
    connectionsActor ! CreateGame(gameName, gamePromise)
    gamePromise.future.map { game =>
      Akka.system.scheduler.scheduleOnce(WaitDurationBeforeRemovingGameIfNotStarted)(removeGame(game.gameId))
    }
    gamePromise.future
  }

  def getGame(gameId: GameId): Future[Option[Game]] = {
    val gamePromise = Promise[Option[Game]]
    connectionsActor ! GetGame(gameId, gamePromise)
    gamePromise.future
  }

  def getAllGames(): Future[Seq[Game]] = {
    val allGamesPromise = Promise[Seq[Game]]
    connectionsActor ! GetAllGames(allGamesPromise)
    allGamesPromise.future
  }

  def removeGame(gameId: GameId): Unit = {
    connectionsActor ! RemoveGame(gameId)
  }
}

class ConnectionsActor extends Actor {
  import ConnectionsActor._

  private var games = Map[GameId, Game]()

  def receive = {
    case CreateGame(gameName, gamePromise) =>
      onCreateGame(gameName, gamePromise)
    case GetGame(gameId, gamePromise) =>
      onGetGame(gameId, gamePromise)
    case GetAllGames(gamesPromise) =>
      onGetAllGames(gamesPromise)
    case RemoveGame(gameId) =>
      onRemoveGame(gameId)
  }

  def onRemoveGame(gameId: GameId) {
    games -= gameId
  }

  def onGetAllGames(gamesPromise: Promise[Seq[Game]]) {
    gamesPromise.success(games.values.toSeq)
  }

  def onCreateGame(gameName: String, gamePromise: Promise[Game]) {
    val game = Game(freeGameId, UUID.randomUUID(), gameName)
    games += (game.gameId -> game)
    gamePromise.success(game)
  }

  def onGetGame(gameId: GameId, gamePromise: Promise[Option[Game]]) {
    gamePromise.success(games.get(gameId))
  }

  def onJoinGame(gameId: GameId, snakeIdPromise: Promise[SnakeId]) {
    games.get(gameId) match {
      case Some(game) => snakeIdPromise.completeWith(game.join())
      case None => snakeIdPromise.failure(new SnakeGameException(s"game with id $gameId does not exist"))
    }
  }

  @tailrec
  private def freeGameId: GameId = {
    val gameId = new GameId(UUID.randomUUID())
    if (games.contains(gameId)) freeGameId
    else gameId
  }
}
