import akka.actor.ActorSystem
import play.api.{Application, GlobalSettings, Logger}
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object Global extends GlobalSettings{

  override def onStart(app: Application) {
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError)
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound)
  }

}