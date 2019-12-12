package net.matlux.socialnetwork

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import net.matlux.socialnetwork.SocialNetworkDomain._
import net.matlux.utils.Atom

import scala.concurrent.Future
import scala.util.Random
import spray.json.DefaultJsonProtocol._

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http

import scala.concurrent.Future
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.scaladsl.{Sink, Source}


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
