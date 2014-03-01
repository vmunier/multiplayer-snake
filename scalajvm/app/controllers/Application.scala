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
      Ok(views.html.games(games.map(_.gameId)))
    }
  }

  def createGame() = Action.async { request =>
    ConnectionsActor.createGame.map { gameId =>
      Redirect(routes.Application.game(gameId.id))
    }
  }

  def game(uuid: UUID) = Action.async { request =>
    withGame(uuid) { game =>
      Ok(views.html.game(game.gameId.id))
    }
  }

  // The first message is
  def joinGame(uuid: UUID) = WebSocket.async[JsValue] { request =>
    withGameWS(uuid) { game =>
      game.join.map { snakeId =>
        val iteratee = Iteratee.foreach[JsValue] { event =>
          Json.fromJson[ClientNotif](event).map { clientNotif =>
            game.moveSnake(snakeId, clientNotif.move)
          }
        }
        game.start()
        val playerSnakeIdEnum: Enumerator[JsValue] = Enumerator(Json.obj("playerSnakeId" -> snakeId))
        (iteratee, playerSnakeIdEnum.andThen(game.notifsEnumerator.map(gameNotif => Json.toJson(gameNotif))))
      }
    }
  }

  def gameJs(uuid: UUID) = Action { implicit request =>
    Ok(views.js.game(uuid))
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
