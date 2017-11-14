package net.matlux

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

class HelloWorldServer(val config: Config,
                       implicit val system : ActorSystem,
                       implicit val materializer: ActorMaterializer,
                       implicit val auction: ActorRef
                      ) extends HelloWorldService {

  //val auction = auction1
  //println("config="+config.getString("http.address")+config.getString("http.port"))
  println("routes="+routes)
  Http().bindAndHandle(routes, config.getString("http.address"), config.getInt("http.port"))

}



object HelloWorldServer {

  // bids
  case class Bid(userId: String, offer: Int)

  case object GetBids

  case class Bids(bids: List[Bid])


  class Auction extends Actor with ActorLogging {
    var bids = List.empty[Bid]

    def receive = {
      case bid@Bid(userId, offer) =>
        bids = bids :+ bid
        log.info(s"Bid complete: $userId, $offer")
      case GetBids => sender() ! Bids(bids)
      case _ => log.info("Invalid message")
    }
  }


  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val config = ConfigFactory.load()
    val logger = Logging(system, getClass)

    val auction = system.actorOf(Props[Auction], "auction")

    new HelloWorldServer(config,system,materializer,auction)

    //println(Order(List(Item("Jose Gonzales2 CD", 5))).toJson)
  }

}
