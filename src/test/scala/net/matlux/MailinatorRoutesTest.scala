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
import cats.effect.IO
import fs2.Stream
import net.matlux.mailinator.MailinatorDomain._
import net.matlux.mailinator.{JsonMarshallers, MailinatorServer}

import scala.collection.immutable.SortedMap


class MailinatorRoutesTest extends  WordSpec with Matchers with JsonMarshallers with ScalatestRouteTest {


  val fixtureEmailJsonInput = """{"from":"a@f","to":"b@f","subject":"topic","content":"blabla"}"""
  val fixtureEmailJsonInput2 = """{"from":"a2@f","to":"b2@f","subject":"topic2","content":"blabla2"}"""
  val fixtureEmailJsonInput3 = """{"from":"a3@f","to":"b3@f","subject":"topic3","content":"blabla3"}"""




//  val fixtureRelationshipGraph = RelationshipGraph("facebook",List(Person("John"), Person("Harry"), Person("Peter"), Person("George"), Person("Anna")),List(Relationship("HasConnection","John","Peter"), Relationship("HasConnection","John","George"), Relationship("HasConnection","Peter","George"), Relationship("HasConnection","Peter","Anna")))
//  val fixtureRelationshipMergedGraph = RelationshipGraph("merged",List(Person("Anna"), Person("George"), Person("Harry"), Person("Peter"), Person("John")),
//    List(Relationship("HasConnection","John","Peter"), Relationship("HasConnection","John","George"), Relationship("HasConnection","Peter","Anna"), Relationship("HasConnection","Peter","George")))
//
//  val fixturePersonConnections = Set(PersonConnections("Anna",1,2), PersonConnections("Peter",3,0), PersonConnections("George",2,1), PersonConnections("Harry",0,0), PersonConnections("John",2,1))
//
//  val fixtureFacebookRelationshipGraph = RelationshipGraph("facebook",
//    List(Person("John"), Person("Harry"), Person("Peter"), Person("George"), Person("Anna")),
//    List(Relationship("HasConnection","John","Peter"),
//      Relationship("HasConnection","John","George"),
//      Relationship("HasConnection","Peter","Anna")))
//
//  val fixtureTwitterRelationshipGraph = RelationshipGraph("twitter",
//    List(Person("John"), Person("Harry"), Person("Peter"), Person("George"), Person("Anna")),
//    List(Relationship("HasConnection","John","Peter"),
//      Relationship("HasConnection","John","George"),
//      Relationship("HasConnection","Peter","George")))

  "The service" should {


    val config = ConfigFactory.load()
    val routes = new MailinatorServer(config,system,materializer).routes

    "As a user, I want to check the health of this service (through Rest)" in {
      Get("/health") ~> routes ~> check {
        responseAs[String] shouldEqual "<h1>service is healthy</h1>"

      }
    }
    "As a user, I want to Create a new, random email address. (through Rest)" in {
      Post("/mailboxes") ~> routes ~> check {
        val value = responseAs[MailboxCreated]
        println("value.emailAddress=" + value.emailAddress)
        value.emailAddress should endWith ("@test.com")

      }
    }

    "As a user, I want to errors to be handled when MailboxNotFoundMessage." in {


      Get("/mailboxes/random@address/messages/1") ~> routes ~> check {
        responseAs[ErrorMsg] shouldEqual ErrorMsg("MailboxNotFoundMessage")

      }


    }

    "As a user, I want to errors to be handled when MailNotFoundMessage." in {

      Post("/mailboxes") ~> routes ~> check {
        val value = responseAs[MailboxCreated]
        println("value.emailAddress=" + value.emailAddress)
        value.emailAddress should endWith ("@test.com")


        Get("/mailboxes/" + value.emailAddress + "/messages/1") ~> routes ~> check {
          responseAs[ErrorMsg] shouldEqual ErrorMsg("MailNotFoundMessage")

        }
      }

    }

    "As a user, I want to post, list and retreive a bunch of emails. (Integration Test)" in {

      Post("/mailboxes") ~> routes ~> check {
        val value = responseAs[MailboxCreated]
        println("value.emailAddress=" + value.emailAddress)
        value.emailAddress should endWith ("@test.com")

        Post("/mailboxes/" + value.emailAddress + "/messages", HttpEntity(ContentTypes.`application/json`, fixtureEmailJsonInput)) ~> routes ~> check {
          val mail = responseAs[Mail]
          mail.content  shouldEqual "blabla"


          Get("/mailboxes/" + value.emailAddress + "/messages/1") ~> routes ~> check {
            val mail = responseAs[Mail]
            mail.content  shouldEqual "blabla"
          }

          Get("/mailboxes/" + value.emailAddress + "/messages?page=1&size=30") ~> routes ~> check {
            val mailsInfo = responseAs[PagedMailsInfo]
            mailsInfo.mails.size  shouldEqual 1

            Post("/mailboxes/" + value.emailAddress + "/messages", HttpEntity(ContentTypes.`application/json`, fixtureEmailJsonInput2)) ~> routes ~> check {
              val mail = responseAs[Mail]
              mail.content  shouldEqual "blabla2"

              Get("/mailboxes/" + value.emailAddress + "/messages?page=1&size=30") ~> routes ~> check {
                val mailsInfo = responseAs[PagedMailsInfo]
                mailsInfo.mails.size  shouldEqual 2

                Post("/mailboxes/" + value.emailAddress + "/messages", HttpEntity(ContentTypes.`application/json`, fixtureEmailJsonInput3)) ~> routes ~> check {
                  val mail = responseAs[Mail]
                  mail.content  shouldEqual "blabla3"
                  Get("/mailboxes/" + value.emailAddress + "/messages?page=1&size=2") ~> routes ~> check {
                    val mailsInfo = responseAs[PagedMailsInfo]
                    mailsInfo.mails.size  shouldEqual 2
                    Get("/mailboxes/" + value.emailAddress + "/messages?page=2&size=2") ~> routes ~> check {
                      val mailsInfo = responseAs[PagedMailsInfo]
                      mailsInfo.mails.size  shouldEqual 1
                      Get("/mailboxes/" + value.emailAddress + "/messages/2") ~> routes ~> check {
                        val mail = responseAs[Mail]
                        mail.content  shouldEqual "blabla2"
                      }
                    }
                  }
                }
              }
            }
          }

        }
      }

    }

    "As a user, I want to Retrieve an index of messages sent to an email address, including sender, subject, " +
      "and id, in recency order. Support cursor-based pagination through the index." in {

      Post("/mailboxes") ~> routes ~> check {
        val value = responseAs[MailboxCreated]
        println("value.emailAddress=" + value.emailAddress)
        value.emailAddress should endWith ("@test.com")
        Get("/mailboxes/" + value.emailAddress + "/messages/1") ~> routes ~> check {
          responseAs[ErrorMsg] shouldEqual ErrorMsg("MailNotFoundMessage")

        }
      }

    }

//    "test random graph degree of connections through Rest" in {
//      Post("/graph-connections", HttpEntity(ContentTypes.`application/json`, fixtureJsonInput)) ~> routes ~> check {
//        responseAs[List[PersonConnections]].toSet shouldEqual fixturePersonConnections
//
//      }
//    }
//
//    "test marshalling through Rest" in {
//      Post("/test-marshalling", HttpEntity(ContentTypes.`application/json`, fixtureJsonInput)) ~> routes ~> check {
//        responseAs[RelationshipGraph] shouldEqual fixtureRelationshipGraph
//
//      }
//    }
//
//    "test extractDeg1AndDeg2Numbers function" in {
//      extractDeg1AndDeg2Numbers(fixtureRelationshipGraph) shouldEqual fixturePersonConnections
//
//    }
//
//    "test merge" in {
//      merge(fixtureFacebookRelationshipGraph,fixtureTwitterRelationshipGraph) shouldEqual fixtureRelationshipMergedGraph
//
//    }
//
//    /*
//    As a user, I want to query how many people are not connected to anyone for the given social network so I know who to propose new connections to.
//
//    Given a social network name Facebook
//    And a full Facebook graph
//    Return count of people with no connections
//     */
//    "As a user, I want to Return count of people with no connections" in {
//      countNumberOfPeopleWithoutConnection(fixtureRelationshipGraph) shouldEqual 1
//
//    }
//    "As a user, I want to Return count of people with no connections (through Rest)" in {
//      Get("/not-connected-people") ~> routes ~> check {
//        responseAs[String] shouldEqual "1"
//
//      }
//    }
//
//    /*
//    As a user, I want to query how many people are connected to a given person by 1 or 2 degrees of separation for all social networks (facebook and twitter) so I understand her/his social influence.
//
//    Given a person name Peter
//    And a Facebook graph for Peter
//    And a Twitter graph for Peter
//    Return count of connections of 1 degree + count of connections of 2 degree
//    */
//    "As a user, I want to Return count of connections of 1 degree + count of connections of 2 degree for Peter" in {
//      degreeOfConnection("Peter",
//        merge(fixtureFacebookRelationshipGraph,fixtureTwitterRelationshipGraph)) shouldEqual
//        Some(PersonConnections("Peter",3,0))
//
//    }
//    "As a user, I want to Return count of connections of 1 degree + count of connections of 2 degree for Peter (through Rest)" in {
//      Get("/degree-of-connection/Peter") ~> routes ~> check {
//        responseAs[PersonConnections] shouldEqual PersonConnections("Peter",3,0)
//      }
//    }
//
//
//    //unhappy paths
//    "As a user, I want to Return None in case the user does not exist" in {
//      degreeOfConnection("Name_do_not_exists",
//        merge(fixtureFacebookRelationshipGraph,fixtureTwitterRelationshipGraph)) shouldEqual
//        None
//
//    }
//    "As a user, I want to Return None in case the user does not exist (through Rest)" in {
//      Get("/degree-of-connection/Name_do_not_exists") ~> routes ~> check {
//        responseAs[PersonConnections] shouldEqual None
//      }
//    }
//
//
    "test scratchpad" in {

      import cats.effect.IO
      import fs2.Stream

      import cats._, cats.data._, cats.implicits._

      // IO
      trait Connection {
        def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit

        // or perhaps
        def readBytesE(onComplete: Either[Throwable,Array[Byte]] => Unit): Unit =
          readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

        override def toString = "<connection>"
      }
      val c = new Connection {
        def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit = {
          Thread.sleep(200)
          onSuccess(Array(0,1,2))
        }
      }
      // c: Connection = <connection>
      val bytes = cats.effect.Async[IO].async[Array[Byte]] { (cb: Either[Throwable,Array[Byte]] => Unit) =>
        c.readBytesE(cb)
      }
      // bytes: cats.effect.IO[Array[Byte]] = IO$1064869761
      import cats.Monad
      import cats.effect.Sync
//      Stream.eval(bytes).map(_.toList).compile.toVector.unsafeRunSync()

      var mails = SortedMap.empty[Int,Message]
      mails.lastKey
      mails = mails + (1 -> Message("a@f","b@f","topic","blabla"))
      if (mails.isEmpty) 1 else mails.lastKey + 1
//      res3 == false
    }
  }


}
