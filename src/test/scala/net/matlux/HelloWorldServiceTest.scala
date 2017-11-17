package net.matlux

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.testkit.{TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import net.matlux.HelloWorldServer.Auction
import org.scalatest.{Matchers, WordSpec, WordSpecLike}

class HelloWorldServiceTest extends  WordSpec with Matchers with ScalatestRouteTest {

  "The service" should {

    //implicit val system = ActorSystem("my-system")
    //implicit val executor = system.dispatcher
    //implicit val materializer = ActorMaterializer()

    val config = ConfigFactory.load()
    val auction = TestActorRef[Auction]
    val routes = new HelloWorldServer(config,system,materializer,auction).routes


    "return a <>bar<> html message for GET requests to the foo path" in {
      // tests:
      Get("/bar") ~> routes ~> check {
        responseAs[String] shouldEqual "<h1>bar</h1>"
      }
    }
  }


}
