package net.matlux.mailinator

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import net.matlux.mailinator.MailinatorDomain._
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
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import net.matlux.mailinator.mailboxService.MailRouter.CreateMailbox

import java.util.UUID


object MailinatorRoutes extends JsonMarshallers{
//  implicit val system: ActorSystem
//  implicit val executor = system.dispatcher
//  implicit val materializer: ActorMaterializer




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


  def getRoutes(mailRouter : ActorRef) : Route = enableCORS {

    path("health") {
      complete {
        HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>service is healthy</h1>")

      }
    } ~
      pathPrefix("mailboxes") {

        extractLog { log =>
          pathEnd {
            post{
              val emailAddress = createRandomEmail()
              mailRouter ! CreateMailbox(emailAddress)
              complete((StatusCodes.Created, MailboxCreated(emailAddress)))
            }
          }
        }
      }




  }

  def createRandomEmail() = {
    s"${UUID.randomUUID().toString}@test.com"
  }


}


//      post {
//        path("graph-connections") {
//          entity(as[RelationshipGraph]) { graph =>
//
//            val personCon = extractDeg1AndDeg2Numbers(graph)
//            complete(personCon)
//
//          }
//        }
//      } ~ post {
//          path("test-marshalling") {
//            entity(as[RelationshipGraph]) { graph =>
//              println(s"graph = $graph")
//              complete(graph)
//            }
//          }
//      } ~ get {
//        path("not-connected-people") {
//          import cats.effect.IO
//
//          val input = IO.fromFuture(IO{getSocialNetwork("facebook")})
//
//          val program: IO[RelationshipGraph] = for {
//            in <- input
//          } yield (in)
//          val graph = program.unsafeRunSync()
//
//          val nb = countNumberOfPeopleWithoutConnection(graph)
//          complete(nb.toString)
//
//        }
//      } ~ get {
//      path("degree-of-connection" / Remaining) { name =>
//
//          import cats.effect.IO
//
//        val facebookGraphMonad = IO.fromFuture(IO{getSocialNetwork("facebook")})
//        val twitterGraphMonad = IO.fromFuture(IO{getSocialNetwork("twitter")})
//
//          val getFacebookGraph: IO[(RelationshipGraph,RelationshipGraph)] = for {
//            facebook <- facebookGraphMonad
//            twitter <- twitterGraphMonad
//          } yield ((facebook,twitter))
//          val (facebookGraph,twitterGraph) = getFacebookGraph.unsafeRunSync()
//
//          val item = degreeOfConnection(name,merge(facebookGraph,twitterGraph))
//          complete(item)
//
//      }
//    }