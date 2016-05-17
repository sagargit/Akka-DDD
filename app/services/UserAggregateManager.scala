package services

import akka.actor.Props
import domains.AggregateRoot.GetState
import domains.UserAggregate
import domains.UserAggregate.{ChangePassword, Initialize, User}
import services.AggregateManager.Command

/**
 * Created by sagar on 3/24/15.
 */

object UserAggregateManager {
  case class RegisterUser(name: String, pass: String) extends Command
  case class GetUser(name: String) extends Command
  case class ChangeUserPassword(id: String, pass: String) extends Command
  def props: Props = Props(new UserAggregateManager)
}

class UserAggregateManager extends AggregateManager {
  import UserAggregateManager._

  def processCommand = {
    case RegisterUser(name, pass) => println(" Reached inside UserAggregateManager...")
      val id = "user-" + name
      processAggregateCommand(id, Initialize(pass))
    case GetUser(name) =>
      val id = "user-" + name
      processAggregateCommand(id, GetState)
    case ChangeUserPassword(id, pass) =>
      processAggregateCommand(id, ChangePassword(pass))

  }
  override def aggregateProps(id: String) = UserAggregate.props(id)

}