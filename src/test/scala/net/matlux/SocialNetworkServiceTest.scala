package net.matlux

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.testkit.{TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import net.matlux.SocialNetworkServer
import net.matlux.SocialNetworkServer.{Person, Relationship, RelationshipGraph}
import org.scalatest.{Matchers, WordSpec, WordSpecLike}

import spray.json.DefaultJsonProtocol._
import spray.json.DefaultJsonProtocol.{jsonFormat1, jsonFormat3}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


class SocialNetworkServiceTest extends  WordSpec with Matchers with JsonMarshallers with ScalatestRouteTest {


  val fixtureJsonInput = """{
      "sn": "facebook",
      "people": [{"name":"Jonh"},{"name":"Harry"},{"name":"Peter"}, {"name": "George"}, {"name": "Anna"}],
      "relationships": [
                      {"type": "HasConnection", "startNode": "John", "endNode": "Peter"},
                      {"type": "HasConnection", "startNode": "John", "endNode": "George"},
                      {"type": "HasConnection", "startNode": "Peter", "endNode": "George"},
                      {"type": "HasConnection", "startNode": "Peter", "endNode": "Anna"}
                  ]
                  }"""

  val fixtureRelationshipGraph = RelationshipGraph("facebook",List(Person("Jonh"), Person("Harry"), Person("Peter"), Person("George"), Person("Anna")),List(Relationship("HasConnection","John","Peter"), Relationship("HasConnection","John","George"), Relationship("HasConnection","Peter","George"), Relationship("HasConnection","Peter","Anna")))


  "The service" should {

    //implicit val system = ActorSystem("my-system")
    //implicit val executor = system.dispatcher
    //implicit val materializer = ActorMaterializer()



    val config = ConfigFactory.load()
//    val auction = TestActorRef[Auction]
    val routes = new SocialNetworkServer(config,system,materializer).routes

    "return a <>bar<> html message for GET requests to the foo path" in {
      // tests:
      Get("/bar") ~> routes ~> check {
        responseAs[String] shouldEqual "<h1>bar</h1>"
      }

    }
    "test marshalling" in {
      Post("/test-marshalling", HttpEntity(ContentTypes.`application/json`, fixtureJsonInput)) ~> routes ~> check {
        responseAs[RelationshipGraph] shouldEqual fixtureRelationshipGraph

      }
    }

    "test connection" in {
      Post("/test-marshalling", HttpEntity(ContentTypes.`application/json`, fixtureJsonInput)) ~> routes ~> check {
        responseAs[RelationshipGraph] shouldEqual fixtureRelationshipGraph

      }
    }
  }


}
