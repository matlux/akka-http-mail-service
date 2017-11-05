package net.matlux

import akka.Done
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http

import scala.concurrent.Future
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import net.matlux.utils.Atom
import spray.json.DefaultJsonProtocol._
import spray.json._


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


  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val logger = Logging(system, getClass)

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
        }

    //println(Order(List(Item("Jose Gonzales2 CD", 5))).toJson)

    Http().bindAndHandle(routes, config.getString("http.address"), config.getInt("http.port"))
  }
}
