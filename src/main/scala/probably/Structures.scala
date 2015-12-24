package probably

import akka.actor.{ActorRef, Props}
import akka.event.Logging
import akka.pattern.AskSupport
import akka.persistence.{PersistentActor, RecoveryCompleted}
import akka.util.Timeout
import probably.structures.StructureFactory

import scala.concurrent.ExecutionContext
import scala.util.Success

case class Create(name:String, config:Config)
case class Get(name:String)
case class Exists(name:String)
case class StructureSettings(expectedInsertions: Int)

case class ForwardTo[T](name:String, command:T)

class Structures(structureFactory:StructureFactory[_],  expectedErrorPercent:Double) extends PersistentActor {
  var structures = Map[String, ActorRef]()
  val logger = Logging(context.system, getClass)

  override def persistenceId: String = "all-structures"

  override def receiveRecover: Receive = {
    case request:Create => sender ! create(request.name, request.config)
    case RecoveryCompleted =>
  }

  override def receiveCommand: Receive = {
    case request:Create => persist(request){request => sender ! create(request.name, request.config)}
    case Get(name) => sender ! structures(name)
    case ForwardTo(name, command) => if(structures contains name) structures(name) forward command else sender ! StructureNotFound
    case Exists(name) => sender ! (structures contains name)
    case m => logger.info(s"Ignoring $m")
  }

  def create(name:String, config:Config) = {
    structures = structures +
        (name -> context.system.actorOf(Props(classOf[Structure], name, structureFactory.create(expectedErrorPercent)), s"${structureFactory.name}-${name}"))

  }
}


class AllStructures(structures:Map[String, ActorRef])(implicit val timeout:Timeout, val context: ExecutionContext) extends AskSupport {
  def create(set: String, name: String, config: Config) = structures(set) ! Create(name, config)

  def addTo(set:String, name:String, key:String) = (structures(set) ? ForwardTo(name, Add(key))).mapTo[Result[Added]]

  def addAllTo(set:String, name:String, keys:List[String]) = structures(set) ! ForwardTo(name, AddAll(keys))

  def getFrom(set:String, name:String, key:String) = (structures(set) ? ForwardTo(name, IsPresent(key))).mapTo[Result[ProbableResult]]

  def statsOf(set:String, name:String) = (structures(set) ? ForwardTo(name, GetStats)).mapTo[Result[Stats]]

  def exists(set:String, name:String) = (structures(set) ? Exists(name)).mapTo[Boolean]
}