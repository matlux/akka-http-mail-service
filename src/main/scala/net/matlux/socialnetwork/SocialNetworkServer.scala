package net.matlux.socialnetwork

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

class SocialNetworkServer(val config: Config,
                          implicit val system : ActorSystem,
                          implicit val materializer: ActorMaterializer
                      ) extends SocialNetworkService {

  //val auction = auction1
  //println("config="+config.getString("http.address")+config.getString("http.port"))
  println("routes="+routes)
  Http().bindAndHandle(routes, config.getString("http.address"), config.getInt("http.port"))

}



object SocialNetworkServer {



  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val config = ConfigFactory.load()
    val logger = Logging(system, getClass)


    new SocialNetworkServer(config,system,materializer)
  }

}
