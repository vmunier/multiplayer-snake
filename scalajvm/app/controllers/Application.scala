package controllers

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import actors.ConnectionsActor
import models.Game
import models.GameNotifJsonImplicits._
import shared.models.IdTypes.GameId
import play.api._
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc._
import shared.models.Moves._
import models.ClientNotif
import shared.models.PlayerSnakeIdNotif
import play.api.data._
import play.api.data.Forms._

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

  val createGameForm = Form(
    single(
      "gameName" -> nonEmptyText))

  def createGame = Action.async { implicit request =>
    def create(gameName: String): Future[SimpleResult] = ConnectionsActor.createGame(gameName).map { game =>
      Redirect(routes.Application.game(game.gameId.id, Some(game.creatorUUID)))
    }

    createGameForm.bindFromRequest.fold(
      errors => Future(BadRequest(errors.errorsAsJson)),
      gameName => create(gameName))
  }

  def startGame(gameUUID: UUID, creatorUUID: UUID) = Action.async { request =>
    withGame(gameUUID) { game =>
      if (game.creatorUUID != creatorUUID) {
        Forbidden(s"$creatorUUID is not the right creator UUID for the game $gameUUID")
      } else {
        game.start()
        Ok(s"Game $gameUUID started")
      }
    }
  }

  def game(uuid: UUID, maybeCreatorUUID: Option[UUID]) = Action.async { request =>
    withGame(uuid) { game =>
      Ok(views.html.game(game.gameId.id, game.name, maybeCreatorUUID))
    }
  }

  def joinGame(uuid: UUID) = WebSocket.async[JsValue] { request =>
    withGameWS(uuid) { game =>
      game.join.map { snakeId =>
        val iteratee = Iteratee.foreach[JsValue] { event =>
          Json.fromJson[ClientNotif](event).map { clientNotif =>
            game.moveSnake(snakeId, clientNotif.move)
          }
        }
        val playerSnakeIdEnum = Enumerator(Json.toJson(PlayerSnakeIdNotif(snakeId)))
        (iteratee, playerSnakeIdEnum.andThen(game.notifsEnumerator))
      }
    }
  }

  def gameJs(gameUUID: UUID, maybeCreatorUUID: Option[UUID]) = Action { implicit request =>
    Ok(views.js.game(gameUUID, maybeCreatorUUID))
  }

  private def withGameWS(uuid: UUID)(interfaces: Game => Future[(Iteratee[JsValue, _], Enumerator[JsValue])]): Future[(Iteratee[JsValue, _], Enumerator[JsValue])] = {
    ConnectionsActor.getGame(new GameId(uuid)).flatMap { maybeGame =>
      maybeGame.map { game =>
        interfaces(game)
      }.getOrElse {
        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue, Unit]((), Input.EOF)
        // Send an error and close the socket
        val enumerator = Enumerator[JsValue](Json.obj("error" -> s"Game $uuid does not exist")).andThen(Enumerator.enumInput(Input.EOF))
        Future((iteratee, enumerator))
      }
    }
  }

  private def withGame(uuid: UUID)(action: Game => SimpleResult): Future[SimpleResult] = {
    ConnectionsActor.getGame(new GameId(uuid)).map { maybeGame =>
      maybeGame.map { game =>
        action(game)
      }.getOrElse(NotFound(s"Game $uuid does not exist"))
    }
  }

}
