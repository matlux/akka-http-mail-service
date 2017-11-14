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
import akka.stream.scaladsl.{Flow, Source}
import akka.util.{ByteString, Timeout}
import net.matlux.utils.Atom
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.duration._
import akka.pattern.ask
import net.matlux.HelloWorldServer.{Bid, Bids, GetBids}

import scala.util.Random


trait HelloWorldService {
  implicit val system: ActorSystem
  implicit val executor = system.dispatcher
  implicit val materializer: ActorMaterializer

  implicit val auction: ActorRef


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




  // these are from spray-json
  implicit val bidFormat = jsonFormat2(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)


  val routesold = path("hello") {
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

  }

    val routes = path("hello") {
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





}

