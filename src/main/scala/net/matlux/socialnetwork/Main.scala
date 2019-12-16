package net.matlux.socialnetwork

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object Main {
  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val config = ConfigFactory.load()
    val logger = Logging(system, getClass)


    new SocialNetworkServer(config,system,materializer)
  }
}
