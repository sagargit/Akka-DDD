package controllers
import domains._
import akka.actor.{ActorRef, ActorSystem}
import play.api._
import play.api.mvc._
import services.UserAggregateManager.RegisterUser
import services.{RequestHandlerCreator, AggregateManager, UserAggregateManager}
import CommonObj._
object Application extends Controller with RequestHandlerCreator{

  def index = Action {

    val userAggregateManager = system.actorOf(UserAggregateManager.props)

    val registerUser = RegisterUser("sagar","password")

    serveRegister(userAggregateManager,registerUser)

    Ok("nothing")

  }

  def serveRegister(actorRef:ActorRef,message : AggregateManager.Command)  =
    handleRegister[UserAggregate.User](actorRef, message)

}