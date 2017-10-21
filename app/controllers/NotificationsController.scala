package controllers

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import models.notification.Notification
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}
import play.libs.Json
import services.NotificationService


class NotificationsController @Inject() (implicit system: ActorSystem, materializer: Materializer)  extends Controller {

  /**
    *
    * Creates a webscoket conection for given user (id), and sends notifications from server.
    *
    * @param  id id of user
    * @return WebSocket.
    */
  def socket(id: Long) = WebSocket.accept[String, String]{ request =>
    ActorFlow.actorRef { out: ActorRef =>
      ScalaNotificationService.registerActor ! (id -> out)
      NotificationActor.props(out)
    }
  }

  /**
    *
    * Changes status of notification to read. (Read == true).
    *
    * @param  id id of notification
    * @return A 201 status.
    */
  def notificationRead(id: Long)= {
    val notification = Option[Notification] (NotificationService.getInstance().getNullable(id))
    notification match {
      case Some(a) =>
        a.setDelivered(true)
        a.update()
        Action {
          Ok("Notification read")
        }
      case None => Action {
        NotFound("Notification of given id not found")
      }
    }

  }
}

object ScalaNotificationService{

  val system = ActorSystem("System")

  val registerActor: ActorRef = system.actorOf(Props[RegisterActor], name = "register")

  def sendNotification(id: Long, n: Notification) = registerActor ! ("Individual", id, Json.toJson(n).toString)

  def update(id: Long) = registerActor ! id

}