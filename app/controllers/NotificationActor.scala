package controllers

import akka.actor._

object NotificationActor {
  def props(out: ActorRef) = Props(new NotificationActor(out))
}

class NotificationActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      println(msg)
      out ! msg
  }
}