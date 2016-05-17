package services

import akka.actor.{Props, ActorRef, Actor, ActorLogging}
import com.sun.xml.internal.ws.client.RequestContext
import domains.AggregateRoot

import scala.reflect.ClassTag

/**
 * Created by sagar on 3/24/15.
 */

object RequestHandler {

  case class RegisterRequestActor[S <: AggregateRoot.State](
                                                             target: ActorRef,
                                                             message: AggregateManager.Command)(implicit tag: ClassTag[S]) extends RequestHandler {

    override def processResult: Receive = {
      case tag(res) => complete(res)
    }
  }

}


trait RequestHandler extends Actor with ActorLogging {
  import context._
  def target: ActorRef
  def message: AggregateManager.Command
  target ! message

  def complete[T <: AnyRef](obj: T) = {
   println(obj)
    stop(self)
  }
  def processResult: Receive
  override def receive = processResult
}

trait RequestHandlerCreator {
  import controllers.CommonObj._
  import RequestHandler._
  def handleRegister[S <: AggregateRoot.State](
                                                target: ActorRef,
                                                message: AggregateManager.Command)(implicit tag: ClassTag[S]) =

    system.actorOf(Props(RegisterRequestActor[S](target, message)))
}
