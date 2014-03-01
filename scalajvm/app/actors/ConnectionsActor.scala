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
import play.api.Play.current
import play.api.libs.concurrent.Akka


object ConnectionsActor {
  case class CreateGame(gamePromise: Promise[Game])
  case class GetGame(gameId: GameId, gamePromise: Promise[Option[Game]])
  case class GetAllGames(gamesPromise: Promise[Seq[Game]])

  val connectionsActor = Akka.system.actorOf(Props[ConnectionsActor])

  def createGame(): Future[Game] = {
    val gamePromise = Promise[Game]
    connectionsActor ! CreateGame(gamePromise)
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
}

class ConnectionsActor extends Actor {
  import ConnectionsActor._

  private var games = Map[GameId, Game]()

  def receive = {
    case CreateGame(gamePromise) =>
      onCreateGame(gamePromise)
    case GetGame(gameId, gamePromise) =>
      onGetGame(gameId, gamePromise)
    case GetAllGames(gamesPromise) =>
      onGetAllGames(gamesPromise)
  }

  def onGetAllGames(gamesPromise: Promise[Seq[Game]]) {
    gamesPromise.success(games.values.toSeq)
  }

  def onCreateGame(gamePromise: Promise[Game]) {
    val game = Game(freeGameId, UUID.randomUUID())
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
