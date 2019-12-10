package net.matlux

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http

import scala.concurrent.Future
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RespondWithDirectives
import akka.stream.scaladsl.{Flow, Source}
import akka.util.{ByteString, Timeout}
import net.matlux.utils.Atom
import spray.json.DefaultJsonProtocol._
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import spray.json._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}

import scala.concurrent.duration._
import akka.pattern.ask
import net.matlux.SocialNetworkServer._

import scala.util.Random
import Math._

import net.matlux.SocialNetworkDomain.{PersonConnections, Relationship, RelationshipGraph,extractDeg1AndDeg2Numbers}

/*trait EnableCORSDirectives extends RespondWithDirectives {


}*/

trait SocialNetworkService extends JsonMarshallers{
  implicit val system: ActorSystem
  implicit val executor = system.dispatcher
  implicit val materializer: ActorMaterializer




/*

 */

  /*
  {
"sn": "facebook",
"people": [{"name":"Jonh"},{"name":"Harry"},{"name":"Peter"}, {"name": "George"}, {"name": "Anna"}],
"relationships": [
    {"type": "HasConnection", "startNode": "John", "endNode": "Peter"},
    {"type": "HasConnection", "startNode": "John", "endNode": "George"},
    {"type": "HasConnection", "startNode": "Peter", "endNode": "George"},
    {"type": "HasConnection", "startNode": "Peter", "endNode": "Anna"}
]
}
   */
  //Unmarshal(Source("{\"name\":\"Jonh\"}")).to[Person]
//  Unmarshal("{\"name\":\"Jonh\"}").to[Person]
//  personFormat.read(Source("{\"name\":\"Jonh\"}"))

  // (fake) async database query api
  var db = Atom(Map[String, Relationship]("John" -> Relationship("HasConnection", "John", "Peter")))

  def fetchItem(itemId: Long): Future[Option[Relationship]] = Future {
    //db.deRef().get(itemId)
    Some(Relationship("HasConnection","John", "Peter"))
  }

  def connections(graph: RelationshipGraph): Future[List[PersonConnections]] = Future {
    List(PersonConnections("Paul", 1, 2))
  }

  // streams are re-usable so we can define it here
  // and use it for every request
  val numbers = Source.fromIterator(() =>
    Iterator.continually(Random.nextInt()))



  private val allowedCorsVerbs = List(
    CONNECT, DELETE, GET, HEAD, OPTIONS,
    PATCH, POST, PUT, TRACE
  )

  private val allowedCorsHeaders = List(
    "X-Requested-With", "content-type", "origin", "accept"
  )

  lazy val enableCORS =
    respondWithHeader(`Access-Control-Allow-Origin`.`null`) &
      respondWithHeader(`Access-Control-Allow-Methods`(allowedCorsVerbs)) &
      respondWithHeader(`Access-Control-Allow-Headers`(allowedCorsHeaders)) &
      respondWithHeader(`Access-Control-Allow-Credentials`(true))


  val routes = enableCORS {

    path("health") {
      complete {
        HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>service is healthy</h1>")

      }
    } ~ path("bar") {
      complete {
        HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>bar</h1>")

      }
    } ~
      get {
        pathPrefix("item" / LongNumber) { id =>
          // there might be no item for a given id
          val maybeItem: Future[Option[Relationship]] = Future {
            Some(Relationship("HasConnection","John", "Peter"))
          }

          onSuccess(maybeItem) {
            case Some(item) => complete(item)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
      post {
        path("connections") {
          entity(as[RelationshipGraph]) { graph =>
            println(s"graph = $graph")

            val item = extractDeg1AndDeg2Numbers(graph)
            complete(item)

          }
        }
      } ~ post {
          path("test-marshalling") {
            entity(as[RelationshipGraph]) { graph =>
              println(s"graph = $graph")
              complete(graph)
            }
          }
        }

  }



}

