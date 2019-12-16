package net.matlux.socialnetwork

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import net.matlux.rest.{WebServer, WebServerConfig}

import scala.concurrent.Future

class SocialNetworkServer(val config: Config,
                          implicit val system : ActorSystem,
                          implicit val materializer: ActorMaterializer
                      ) extends SocialNetworkService {

  //val auction = auction1
  //println("config="+config.getString("http.address")+config.getString("http.port"))
  println("routes="+routes)
//  Http().bindAndHandle(routes, config.getString("http.address"), config.getInt("http.port"))

  val server = new WebServer(WebServerConfig(8080, "0.0.0.0"),routes)

  def close(): Future[Unit] = server.close()
  def started: Future[Unit] = server.started
}




