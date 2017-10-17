package controllers.notification

import akka.actor.{Actor, ActorRef}
import com.avaje.ebean.Model
import models.notification.Notification
import models.user.User
import play.libs.Json
import services.NotificationService
import scala.collection.JavaConversions._


/*In memory register service for active web socket connections that is represented as an actor */
class RegisterActor extends Actor{

  /*Map with active connections with users.*/
  var active: Map[Long, ActorRef] = Map()


  def receive = {

    case add_user: (Long, ActorRef) =>
      active = active + (add_user._1 -> add_user._2)
      self ! add_user._1

    /*Sends notifications to given user_id*/
    case user_id: (Long) =>
      val notificationService = new NotificationService
      val list = notificationService.getUndeliveredByUser(user_id)
      println(list.map(e => e.getMessage))
      for ((e: Notification) <- list) active(user_id) ! Json.toJson(e).toString
  }
}


