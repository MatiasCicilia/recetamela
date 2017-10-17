package controllers.notification

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}


class NotificationsController @Inject() (implicit system: ActorSystem, materializer: Materializer)  extends Controller {

  def socket(id: Long): WebSocket = WebSocket.accept[String, String]{ request =>
    ActorFlow.actorRef { out: ActorRef =>
      NotificationService.registerActor ! (id -> out)
      NotificationActor.props(out)
    }
  }

  /*def notificationRead(id: Long)= {
  /* convert to optional in case of null. Null check necessary */
    val notif = Notification.find.byId(id)
    notif.setNotificationRead(true)
    notif.save()
    OK
  }*/
}

object NotificationService{

  val system = ActorSystem("System")

  /* Register with all active connections of users.*/
  val registerActor: ActorRef = system.actorOf(Props[RegisterActor], name = "register")

  def update(id: Long) = registerActor ! id

}