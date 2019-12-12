package net.matlux

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.testkit.{TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec, WordSpecLike}
import spray.json.DefaultJsonProtocol._
import spray.json.DefaultJsonProtocol.{jsonFormat1, jsonFormat3}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import net.matlux.socialnetwork.SocialNetworkDomain._
import net.matlux.socialnetwork.{JsonMarshallers, SocialNetworkServer}


class SocialNetworkServiceTest extends  WordSpec with Matchers with JsonMarshallers with ScalatestRouteTest {


  val fixtureJsonInput = """{
      "sn": "facebook",
      "people": [{"name":"John"},{"name":"Harry"},{"name":"Peter"}, {"name": "George"}, {"name": "Anna"}],
      "relationships": [
                      {"type": "HasConnection", "startNode": "John", "endNode": "Peter"},
                      {"type": "HasConnection", "startNode": "John", "endNode": "George"},
                      {"type": "HasConnection", "startNode": "Peter", "endNode": "George"},
                      {"type": "HasConnection", "startNode": "Peter", "endNode": "Anna"}
                  ]
                  }"""

  val fixtureRelationshipGraph = RelationshipGraph("facebook",List(Person("John"), Person("Harry"), Person("Peter"), Person("George"), Person("Anna")),List(Relationship("HasConnection","John","Peter"), Relationship("HasConnection","John","George"), Relationship("HasConnection","Peter","George"), Relationship("HasConnection","Peter","Anna")))

  val fixturePersonConnections = Set(PersonConnections("Anna",1,2), PersonConnections("Peter",3,0), PersonConnections("George",2,1), PersonConnections("Harry",0,0), PersonConnections("John",2,1))


  "The service" should {


    val config = ConfigFactory.load()
    val routes = new SocialNetworkServer(config,system,materializer).routes


    "test connections through Rest" in {
      Post("/connections", HttpEntity(ContentTypes.`application/json`, fixtureJsonInput)) ~> routes ~> check {
        responseAs[List[PersonConnections]].toSet shouldEqual fixturePersonConnections

      }
    }

    "test marshalling through Rest" in {
      Post("/test-marshalling", HttpEntity(ContentTypes.`application/json`, fixtureJsonInput)) ~> routes ~> check {
        responseAs[RelationshipGraph] shouldEqual fixtureRelationshipGraph

      }
    }

    "test connections" in {
      extractDeg1AndDeg2Numbers(fixtureRelationshipGraph) shouldEqual fixturePersonConnections

    }

    "test scratchpad" in {

      extractRelationships(fixtureRelationshipGraph,"John")

      allThePeople(fixtureRelationshipGraph).map{p =>
        val relationshipsDeg1 = extractRelationships(fixtureRelationshipGraph,p)
        (p,relationshipsDeg1,relationshipsDeg1.flatMap(extractRelationships(fixtureRelationshipGraph,_)).filter(_ != p).diff(relationshipsDeg1))}.
        map{case (name,relDeg1,relDeg2) => (name, relDeg1, relDeg2)}

      extractDeg1AndDeg2(fixtureRelationshipGraph)
      extractDeg1AndDeg2Numbers(fixtureRelationshipGraph)

      for {
        people <- allThePeople(fixtureRelationshipGraph)
        relationshipsDeg1 <- extractRelationships(fixtureRelationshipGraph,people)
        relationshipsDeg2 <- extractRelationships(fixtureRelationshipGraph,relationshipsDeg1)

        if (people != relationshipsDeg2 && !extractRelationships(fixtureRelationshipGraph,relationshipsDeg1).contains(relationshipsDeg1))
      } yield (people,(extractRelationships(fixtureRelationshipGraph,relationshipsDeg1).contains(relationshipsDeg1)))
      peopleWithoutRelationships(fixtureRelationshipGraph)

      peopleWithRelationships(fixtureRelationshipGraph)
      extractRelationships(fixtureRelationshipGraph,"ohn")

    }
  }


}
