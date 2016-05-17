package domains

import akka.actor.Props
import akka.persistence.SnapshotMetadata
import domains.AggregateRoot._

/**
 * Created by sagar on 3/24/15.
 */
object UserAggregate {
  import AggregateRoot._
  case class User(id: String, pass: String = "") extends State
  case class Initialize(pass: String) extends Command
  case class ChangePassword(pass: String) extends Command
  case class UserInitialized(pass: String) extends Event
  case class UserPasswordChanged(pass: String) extends Event
  case object UserRemoved extends Event
  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends AggregateRoot {
  import UserAggregate._
  override def persistenceId = id
  override def updateState(evt: AggregateRoot.Event): Unit = evt match {
    case UserInitialized(pass) =>
      println("reached here")
      context.become(created)
      state = User(id, pass)
      println("reached here...2")
    case UserPasswordChanged(newPass) =>
      state match {
        case s: User => state = s.copy(pass = newPass)
        case _ => //nothing
      }
    case UserRemoved =>
      context.become(removed)
      state = Removed
  }
  val initial: Receive = {
    case Initialize(pass) =>
      val encryptedPass = pass+"123"
      persist(UserInitialized(encryptedPass)){
        event =>
          println("reached here..3")
          updateState(event)
          println("reached here...4")
          afterEventPersisted(event)
      }
      // here we created an Event using a Command. This method is called from Aggregate Manger.
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }
  val created: Receive = {
    case Remove =>
      persist(UserRemoved)(afterEventPersisted)
    case ChangePassword(newPass) =>
      val newPassEncrypted = newPass +"123"
      persist(UserPasswordChanged(newPassEncrypted))(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }
  val removed: Receive = {
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }
  val receiveCommand: Receive = initial


  override def restoreFromSnapshot(metadata: SnapshotMetadata, state: AggregateRoot.State) = {
    this.state = state
    state match {
      case Uninitialized => context become initial
      case Removed => context become removed
      case _: User => context become created
    }
  }
}