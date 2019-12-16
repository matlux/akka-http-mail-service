package net.matlux.rest

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer

import scala.concurrent.Future

case class WebServerConfig(restPort: Int, host: String = "0.0.0.0")

class WebServer(conf: WebServerConfig, endpoints: Route*)(implicit system: ActorSystem, materializer: ActorMaterializer) {
  private implicit val ec = system.dispatcher
  private val binding = Http().bindAndHandle(
    DebuggingDirectives.logRequestResult("REQUEST", Logging.DebugLevel)(RouteConcatenation.concat(endpoints: _*)), conf.host, conf.restPort
  )

  def close(): Future[Unit] = binding.flatMap(_.unbind())
  def started: Future[Unit] = binding.map(_ => ())
}