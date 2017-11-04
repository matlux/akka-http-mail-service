package net.matlux

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.server.Directives._


object HelloWorldService {
  implicit val system = ActorSystem("my-system")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

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
        }}


        Http().bindAndHandle(routes, config.getString("http.address"), config.getInt("http.port"))
  }
}
