package controllers

import akka.actor.ActorSystem

/**
 * Created by sagar on 3/24/15.
 */
object CommonObj {
  val system = ActorSystem("sagar")
}

case class Error(message: String)

case class Acknowledge(id: String)