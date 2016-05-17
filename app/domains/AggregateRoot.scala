package domains

import akka.actor.ActorLogging
import akka.persistence.{SnapshotMetadata, SnapshotOffer, PersistentActor}
import controllers.Acknowledge
import domains.UserAggregate.User

/**
 * Created by sagar on 3/24/15.
 */
object AggregateRoot {
  trait State
  case object Uninitialized extends State
  case object Removed extends State
  trait Event
  trait Command
  case object Remove extends Command
  case object GetState extends Command
  /**
   * We don't want the aggregate to be killed if it hasn't fully restored yet,
   * thus we need some non AutoReceivedMessage that can be handled by akka persistence.
   */
  case object KillAggregate extends Command
  /**
   * Specifies how many events should be processed before new snapshot is taken.
   */
  val eventsPerSnapshot = 10
}
trait AggregateRoot extends PersistentActor with ActorLogging {
  import AggregateRoot._
  override def persistenceId: String
  protected var state: State = Uninitialized
  private var eventsSinceLastSnapshot = 0
  /**
   * Updates internal processor state according to event that is to be applied.
   *
   * @param evt Event to apply
   */
  def updateState(evt: Event): Unit
  /**
   * This method should be used as a callback handler for persist() method.
   *
   * @param evt Event that has been persisted
   */
  protected def afterEventPersisted(evt: Event): Unit = {
    println(" inside after Event persisted (Aggregate Root)")
    eventsSinceLastSnapshot += 1
    if (eventsSinceLastSnapshot >= eventsPerSnapshot) {
      log.debug("{} events reached, saving snapshot", eventsPerSnapshot)
      saveSnapshot(state)
      eventsSinceLastSnapshot = 0
    }
    updateAndRespond(evt)
//    sender ! "something sent"
  }
  private def updateAndRespond(evt: Event): Unit = {
    updateState(evt)
    respond()
  }
  protected def respond(): Unit = {
    println("Inside respond.......")
    sender() ! state
//    context.parent ! Acknowledge(persistenceId)
  }
  private def publish(event: Event) =
    context.system.eventStream.publish(event)  // here we can publish required events
  override val receiveRecover: Receive = {
    case evt: Event =>
      eventsSinceLastSnapshot += 1
      updateState(evt)
    case SnapshotOffer(metadata, state: State) =>
      restoreFromSnapshot(metadata, state)
      log.debug("recovering aggregate from snapshot")
  }
  protected def restoreFromSnapshot(metadata: SnapshotMetadata, state: State)
}