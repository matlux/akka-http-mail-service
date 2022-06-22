package net.matlux.mailinator
import java.time.Instant


import scala.collection.Set
import scala.collection.immutable.SortedMap

object MailinatorDomain {


  // domain model
  type EmailAddress = String
  final case class MailboxCreated(emailAddress: String)
  final case class Message(from: String, to: String, subject: String, content : String)
  final case class Mail(id :Int, datetime : Long, from: String, to: String, subject: String, content : String)
  final case class MailMetaData(id :Int, datetime : Long, from: String, to: String, subject: String)
  final case class PagedMailsInfo(page: Int,
                                  nextPage: Option[Int],
                                  numberOfPages : Int,
                                  numberOfMails : Int,
                                  mails : Vector[MailMetaData])

  // error messages
  final case class ErrorMsg(message: String)








//  final case class Relationship(`type`: String, startNode: String, endNode: String)
//  final case class Person(name: String)
//  final case class RelationshipGraph(sn: String, people: List[Person], relationships: List[Relationship])
//  final case class PersonConnections(name: String, degree1 : Int, degree2 : Int)
//
//  // domain functions
//  def peopleWithRelationships(relationshipGraph : RelationshipGraph) ={
//    for {
//      rel : Relationship <- relationshipGraph.relationships.toSet
//      relationShipPeople <- Set(rel.startNode,rel.endNode)
//    } yield(relationShipPeople)
//  }
//
//  def peopleWithoutRelationships(relationshipGraph : RelationshipGraph) ={
//    relationshipGraph.people.map(_.name).toSet.diff( peopleWithRelationships(relationshipGraph))
//  }
//
//  def allThePeople(relationshipGraph : RelationshipGraph) ={
//    relationshipGraph.people.map(_.name).toSet
//  }
//
//  def extractRelationships(relationshipGraph: RelationshipGraph,person : String): Set[String] = {
//    (for {
//      rel: Relationship <- relationshipGraph.relationships.toSet
//      linkedPerson <- Set(rel.startNode, rel.endNode)
//
//      if (rel.startNode == person || rel.endNode == person)
//    } yield (linkedPerson)).diff(Set(person))
//  }
//
//
//  def extractDeg1AndDeg2(relationshipGraph : RelationshipGraph) ={
//    allThePeople(relationshipGraph).map{p =>
//      val relationshipsDeg1 = extractRelationships(relationshipGraph,p)
//      (p,relationshipsDeg1,relationshipsDeg1.flatMap(extractRelationships(relationshipGraph,_)).filter(_ != p).diff(relationshipsDeg1))}
//  }
//
//  def degreeOfConnection(name: String, relationshipGraph : RelationshipGraph) : Option[PersonConnections]   ={
//    val connectionses: Set[PersonConnections] = extractDeg1AndDeg2(relationshipGraph).
//      filter { case (nameInternal, relDeg1, relDeg2) => nameInternal == name }.
//      map { case (name, relDeg1, relDeg2) => PersonConnections(name, relDeg1.size, relDeg2.size) }
//    connectionses.toList match {
//      case List(person) => Some(person)
//      case Nil => None
//    }
//  }
//
//  def extractDeg1AndDeg2Numbers(relationshipGraph : RelationshipGraph) : Set[PersonConnections] ={
//    extractDeg1AndDeg2(relationshipGraph).
//      map{case (name,relDeg1,relDeg2) => PersonConnections(name, relDeg1.size, relDeg2.size)}
//  }
//
//
//  def countNumberOfPeopleWithoutConnection(relationshipGraph : RelationshipGraph) : Int ={
//    peopleWithoutRelationships(relationshipGraph).size
//  }
//
//  def merge(rel1 : RelationshipGraph,rel2 : RelationshipGraph) = {
//    new RelationshipGraph("merged",
//      (rel1.people.toSet ++ rel2.people.toSet).toList,
//      (rel1.relationships.toSet ++ rel2.relationships.toSet).toList)
//  }

}
