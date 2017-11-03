package net.matlux

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{ConfigFactory, Config}
import akka.http.scaladsl.server.Directives._


object HelloWorldService {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val logger = Logging(system, getClass)
  
    val routes =
      path("hello") {
        get {
          complete {
            "Hello, World!"
          }
        }
      }
  
    Http().bindAndHandle(routes, config.getString("http.address"), config.getInt("http.port"))
  }
}
