package controllers

import java.util.UUID

import actors.ConnectionsActor
import models.ClientNotif._
import models.{ClientNotif, Game}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Promise
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import shared.models.GameNotifJsonImplicits._
import shared.models.IdTypes.GameId
import shared.models.{GameNotif, Heartbeat, PlayerSnakeIdNotif}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.games)
  }

  case class Test(s: Set[Int])

  def test() = Action {
    import play.api.libs.json._

    implicit val testFormat = Json.format[Test]
    val json = Json.toJson(Test(Set(1)))
    Ok(json)
  }

  def games = Action.async {
    ConnectionsActor.getAllGames.map { games =>
      Ok(views.html.games(games.map(game => (game.gameId, game.name))))
    }
  }

  val createGameForm = Form(single("gameName" -> nonEmptyText))

  def createGame = Action.async { implicit request =>
    def create(gameName: String): Future[Result] = ConnectionsActor.createGame(gameName).map { game =>
      Redirect(routes.Application.game(game.gameId.id, Some(game.creatorUUID)))
    }

    createGameForm.bindFromRequest.fold(errors => Future(BadRequest(errors.errorsAsJson)), gameName => create(gameName))
  }

  def startGame(gameUUID: UUID, creatorUUID: UUID) = Action.async { request =>
    withGame(gameUUID) { game =>
      if (game.creatorUUID != creatorUUID) {
        Forbidden(s"$creatorUUID is not the right creator UUID for the game $gameUUID")
      } else {
        for (started <- game.start if started) {
          ConnectionsActor.removeGame(game.gameId)
        }

        Ok(s"Game $gameUUID started")
      }
    }
  }

  def game(uuid: UUID, maybeCreatorUUID: Option[UUID]) = Action.async { request =>
    withGame(uuid) { game =>
      Ok(views.html.game(game.gameId.id, game.name, maybeCreatorUUID))
    }
  }

  def joinGame(uuid: UUID, maybeCreatorUUID: Option[UUID]) = WebSocket.tryAccept { request =>
    tryWithGame(uuid) { game =>
      game.join.map { snakeId =>
        val iteratee = Iteratee.foreach[JsValue] { event =>
          Json.fromJson[ClientNotif](event).map { clientNotif =>
            game.moveSnake(snakeId, clientNotif.move)
          }

        }.map { _ =>
          game.disconnectSnake(snakeId)
          maybeCreatorUUID.foreach { _ =>
            ConnectionsActor.removeGame(game.gameId)
            game.stopIfNotStarted()
          }
        }
        val playerSnakeIdEnum: Enumerator[GameNotif] = Enumerator(PlayerSnakeIdNotif(snakeId))
        (iteratee, playerSnakeIdEnum.andThen(game.notifsEnumerator).map(_.toJson))
      }
    }
  }

  def gameJs(gameUUID: UUID, maybeCreatorUUID: Option[UUID]) = Action { implicit request =>
    Ok(views.js.game(gameUUID, maybeCreatorUUID))
  }

  private def tryWithGame[A](uuid: UUID)(action: Game => Future[(Iteratee[JsValue, _], Enumerator[JsValue])]): Future[Either[Result, (Iteratee[JsValue, _], Enumerator[JsValue])]] = ConnectionsActor.getGame(new GameId(uuid)).flatMap {
    case Some(game) => action(game).map(Right(_))
    case None => Future(Left(NotFound(s"Game $uuid does not exist")))
  }

  private def withGame(uuid: UUID)(action: Game => Result): Future[Result] = {
    ConnectionsActor.getGame(new GameId(uuid)).map { maybeGame =>
      maybeGame.map { game =>
        action(game)
      }.getOrElse(NotFound(s"Game $uuid does not exist"))
    }
  }
}
