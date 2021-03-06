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
import akka.util.Timeout
import net.matlux.mailinator.mailboxService.MailRouter.{CreateMailbox, GetEmailByIndex, GetEmailByPage, MailNotFound, MailboxNotFound, PostEmail}

import scala.concurrent.duration._
import java.util.UUID


object MailinatorRoutes extends JsonMarshallers{
//  implicit val system: ActorSystem
//  implicit val executor = system.dispatcher
//  implicit val materializer: ActorMaterializer
  final val DefaultPageSize = 10

  implicit val timeout: Timeout            = Timeout(2.seconds)


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
          } ~
            pathPrefix(Segment) { emailAddress =>
              pathPrefix("messages") {
                pathEnd {
                  post {
                    entity(as[Message]) { newMail =>
                      onSuccess(mailRouter ? PostEmail(emailAddress, newMail)) {
                        case persistedMail: Mail =>
                          log.info("CREATED Mail by id: {} for Mailbox: {}",
                            persistedMail.id,
                            emailAddress)
                          complete((StatusCodes.Created, persistedMail))
                        case MailboxNotFound => complete(StatusCodes.NotFound, ErrorMsg("MailboxNotFoundMessage"))
                      }
                    }
                  } ~
                    get {
                      parameter('page ? 1, 'size ? DefaultPageSize) { (page, size) =>
                        onSuccess(mailRouter ? GetEmailByPage(emailAddress, page, size)) {
                          case mails: PagedMailsInfo => complete((StatusCodes.OK, mails))
                          case MailboxNotFound   => complete(StatusCodes.NotFound, ErrorMsg("MailboxNotFoundMessage"))
                        }
                      }
                    }
                } ~
                  pathPrefix(IntNumber) { messageId =>
                    pathEnd {

                        get {
                          //complete("ok get messageId" + messageId)
                          onSuccess(mailRouter ? GetEmailByIndex(emailAddress, messageId)) {
                            case mail: Mail      => complete((StatusCodes.OK, mail))
                            case MailboxNotFound => complete(StatusCodes.NotFound, ErrorMsg("MailboxNotFoundMessage"))
                            case MailNotFound    => complete(StatusCodes.NotFound, ErrorMsg("MailNotFoundMessage"))
                          }
                        } ~
                          delete {
                            complete("ok delete messageId " + messageId)
                          }
                    }
                  }
              } ~
                pathEnd {
                  complete("ok delete emailAddress " + emailAddress)
                }
            }
        }
      }


  }

  def createRandomEmail() = {
    s"${UUID.randomUUID().toString}@test.com"
  }


}
