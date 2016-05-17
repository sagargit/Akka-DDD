package services

import akka.actor._
import domains.AggregateRoot
import domains.AggregateRoot.KillAggregate
import services.AggregateManager.PendingCommand

/**
 * Created by sagar on 3/24/15.
 */
object AggregateManager {
  trait Command
  val maxChildren = 40
  val childrenToKillAtOnce = 20
  case class PendingCommand(sender: ActorRef, targetProcessorId: String, command: AggregateRoot.Command)
}

trait AggregateManager extends Actor with ActorLogging{

  import AggregateManager._
  import scala.collection.immutable._
  private var childrenBeingTerminated: Set[ActorRef] = Set.empty
  private var pendingCommands: Seq[PendingCommand] = Nil

  def processCommand: Receive
  override def receive = processCommand orElse defaultProcessCommand
  private def defaultProcessCommand: Receive = {
    case Terminated(actor) =>
      childrenBeingTerminated = childrenBeingTerminated filterNot (_ == actor)
      val (commandsForChild, remainingCommands) = pendingCommands partition (_.targetProcessorId == actor.path.name)
      pendingCommands = remainingCommands
      log.debug("Child termination finished. Applying {} cached commands.", commandsForChild.size)
      for (PendingCommand(commandSender, targetProcessorId, command) <- commandsForChild) {
        val child = findOrCreate(targetProcessorId)
        child.tell(command, commandSender)
      }
  }
  /**
   * Processes aggregate command.
   * Creates an aggregate (if not already created) and handles commands caching while aggregate is being killed.
   *
   * @param aggregateId Aggregate id
   * @param command Command that should be passed to aggregate
   */
  def processAggregateCommand(aggregateId: String, command: AggregateRoot.Command) = {
    println("inside AggregateManager")
    val maybeChild = context child aggregateId
    maybeChild match {
      case Some(child) if childrenBeingTerminated contains child => println("1")
        log.debug("Received command for aggregate currently being killed. Adding command to cache.")
        pendingCommands :+= PendingCommand(sender(), aggregateId, command)
      case Some(child) => println("2")
        child forward command
      case None => println("3")
        val child = create(aggregateId)
        child forward command
    }
  }
  protected def findOrCreate(id: String): ActorRef =
    context.child(id) getOrElse create(id)
  protected def create(id: String): ActorRef = {
    killChildrenIfNecessary()
    val agg = context.actorOf(aggregateProps(id), id)
    context watch agg //  receive a Terminated(subject) message when watched actor is terminated.
    agg
  }
  /**
   * Returns Props used to create an aggregate with specified id
   *
   * @param id Aggregate id
   * @return Props to create aggregate
   */
  def aggregateProps(id: String): Props
  private def killChildrenIfNecessary() = {
    val childrenCount = context.children.size - childrenBeingTerminated.size
    if (childrenCount >= maxChildren) {
      log.debug(s"Max manager children exceeded. Killing ${childrenToKillAtOnce} children.")
      val childrenNotBeingTerminated = context.children.filterNot(childrenBeingTerminated.toSet)
      val childrenToKill = childrenNotBeingTerminated take childrenToKillAtOnce
      childrenToKill foreach (_ ! KillAggregate)
      childrenBeingTerminated ++= childrenToKill
    }
  }

}