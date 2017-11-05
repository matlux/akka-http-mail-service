package net.matlux

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http

import scala.concurrent.Future
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import net.matlux.utils.Atom
import spray.json.DefaultJsonProtocol._
import spray.json._
import scala.concurrent.duration._
import akka.pattern.ask
import scala.util.Random


object HelloWorldService {
  implicit val system = ActorSystem("my-system")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()


  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)


  // domain model
  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])

  // (fake) async database query api
  var db = Atom(Map[Long, Item](4L -> Item("Jose Gonzales CD", 4)))

  def fetchItem(itemId: Long): Future[Option[Item]] = Future {
    db.deRef().get(itemId)
  }

  def saveOrder(order: Order): Future[Done] = Future {
    db.swap { currentDb: Map[Long, Item] =>
      val newMap = order.items.foldLeft(Map[Long, Item]()) { (acc: Map[Long, Item], item: Item) =>
        acc + (item.id -> item)
      }
      currentDb ++ newMap
    }
    Done
  }

  // streams are re-usable so we can define it here
  // and use it for every request
  val numbers = Source.fromIterator(() =>
    Iterator.continually(Random.nextInt()))

  // bids
  case class Bid(userId: String, offer: Int)
  case object GetBids
  case class Bids(bids: List[Bid])

  class Auction extends Actor with ActorLogging {
    var bids = List.empty[Bid]
    def receive = {
      case bid @ Bid(userId, offer) =>
        bids = bids :+ bid
        log.info(s"Bid complete: $userId, $offer")
      case GetBids => sender() ! Bids(bids)
      case _ => log.info("Invalid message")
    }
  }

  // these are from spray-json
  implicit val bidFormat = jsonFormat2(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val logger = Logging(system, getClass)

    val auction = system.actorOf(Props[Auction], "auction")

    val routes =

      path("hello") {
        get {
          complete {
            HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")
          }
        } ~
          post {
            complete {
              HttpEntity(ContentTypes.`application/json`, "{\"say\" :\"hello post to akka-http\"}")
            }
          }

      } ~ path("bar") {
        complete {
          HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>bar</h1>")
        }
      } ~
        get {
          pathPrefix("item" / LongNumber) { id =>
            // there might be no item for a given id
            val maybeItem: Future[Option[Item]] = fetchItem(id)

            onSuccess(maybeItem) {
              case Some(item) => complete(item)
              case None => complete(StatusCodes.NotFound)
            }
          }
        } ~
        post {
          path("create-order") {
            entity(as[Order]) { order =>
              val saved: Future[Done] = saveOrder(order)
              onComplete(saved) { done =>
                complete("order created")
              }
            }
          }
        } ~
        path("random") {
          get {
            complete(
              HttpEntity(
                ContentTypes.`text/plain(UTF-8)`,
                // transform each number to a chunk of bytes
                numbers.map(n => ByteString(s"$n\n"))
              )
            )
          }
        } ~
        path("auction") {
          put {
            parameter("bid".as[Int], "user") { (bid, user) =>
              // place a bid, fire-and-forget
              auction ! Bid(user, bid)
              complete((StatusCodes.Accepted, "bid placed"))
            }
          } ~
            get {
              implicit val timeout: Timeout = 5.seconds

              // query the actor for the current auction state
              val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
              complete(bids)
            }
        }

    //println(Order(List(Item("Jose Gonzales2 CD", 5))).toJson)

    Http().bindAndHandle(routes, config.getString("http.address"), config.getInt("http.port"))
  }
}
