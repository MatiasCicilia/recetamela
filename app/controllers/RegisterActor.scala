package controllers

import akka.actor.{Actor, ActorRef}
import models.notification.Notification
import play.libs.Json
import services.NotificationService

import scala.collection.JavaConversions._


/*In memory register service for active web socket connections that is represented as an actor */
class RegisterActor extends Actor{

  /*Map with active connections with users.*/
  var active: Map[Long, ActorRef] = Map()


  def receive = {

    /*adds user and sends all unread notifications*/
    case add_user: (Long, ActorRef) =>
      active = active + (add_user._1 -> add_user._2)
      self ! add_user._1

    /*Sends all unread notifications to given user_id*/
    case user_id: Long =>
      val list = NotificationService.getInstance().getUndeliveredByUser(user_id)
      for ((e: Notification) <- list) active(user_id) ! Json.toJson(e).toString

    /*Sends individual notification to user_id*/
    case tuple: (_ , Long, String) =>
      if (active.contains(tuple._2)) active(tuple._2) ! tuple._3
  }
}

/*Agregar Logout, atrapar potencial exception en el ultimo case si no hay usuario en el mapa.*/


