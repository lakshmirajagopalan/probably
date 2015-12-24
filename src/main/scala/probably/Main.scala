package probably

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import akka.pattern.AskSupport
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import probably.structures.{BloomFilterFactory, HyperLogLogFactory}
import spray.json.DefaultJsonProtocol._

case class NotFoundMessage(name:String,message: String = "Not found")
case class Config(expectedError:Double, expectedInsertions:Long)

trait Protocols {
  implicit val addedFormat = jsonFormat1(Added.apply)
  implicit val probableResultFormat = jsonFormat2(ProbableResult.apply)
  implicit val statsFormat = jsonFormat2(Stats.apply)
  implicit val notFoundFormat = jsonFormat2(NotFoundMessage.apply)
  implicit val configFormat = jsonFormat2(Config.apply)
}


object Main extends App with AskSupport with Protocols {
  implicit val system = ActorSystem("probably")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(100, TimeUnit.SECONDS)

  val config = new Settings(ConfigFactory.load())
  val structures = Map(
    "bloom" -> system.actorOf(Props(classOf[Structures], new BloomFilterFactory, 3.0)),
    "hll" -> system.actorOf(Props(classOf[Structures], new HyperLogLogFactory, 3.0))
  )
  val allStructures = new AllStructures(structures)
  val setSegment = structures.keys.zip(structures.keys).toMap


  val routes:Route = path(setSegment/Segment) { (set,name) =>
      (post & entity(as[Config])){ config =>
        complete{
          (allStructures exists(set, name)).map[ToResponseMarshallable] { exists =>
            if(exists) Conflict else { allStructures create(set,name, config); Created}
          }
        }
      } ~ (put & entity(as[List[String]])) { keys =>
        complete {
          allStructures addAllTo(set, name, keys)
          Accepted
        }
      } ~ get {
        complete {
          (allStructures statsOf (set,name)).map[ToResponseMarshallable] {
            case Ok(stats) => stats
            case StructureNotFound => NotFound -> NotFoundMessage(name)
          }
        }
      }
    }~path(setSegment/Segment/Segment) { (set, name,key)=> {
          put {
            complete {
              (allStructures addTo (set, name, key)).map[ToResponseMarshallable] {
                case Ok(result) => result
                case StructureNotFound => NotFound -> NotFoundMessage(name)
              }
            }
          } ~ get {
              complete {
                (allStructures getFrom (set, name, key)).map[ToResponseMarshallable] {
                  case Ok(isPresent) => isPresent
                  case StructureNotFound => NotFound -> NotFoundMessage(name)
                }
              }
            }
          }
        }

  Http().bindAndHandle(Route.handlerFlow(routes), config.httpHost, config.httpPort)
}
