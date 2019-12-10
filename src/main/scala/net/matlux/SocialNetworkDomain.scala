package net.matlux

object SocialNetworkDomain {


  // domain model
  final case class Relationship(`type`: String, startNode: String, endNode: String)
  final case class Person(name: String)
  final case class RelationshipGraph(sn: String, people: List[Person], relationships: List[Relationship])
  final case class PersonConnections(name: String, degree1 : Int, degree2 : Int)

  // domain functions
  def peopleWithRelationships(relationshipGraph : RelationshipGraph) ={
    for {
      rel : Relationship <- relationshipGraph.relationships.toSet
      relationShipPeople <- Set(rel.startNode,rel.endNode)
    } yield(relationShipPeople)
  }

  def peopleWithoutRelationships(relationshipGraph : RelationshipGraph) ={
    relationshipGraph.people.map(_.name).toSet.diff( peopleWithRelationships(relationshipGraph))
  }

  def allThePeople(relationshipGraph : RelationshipGraph) ={
    relationshipGraph.people.map(_.name).toSet
  }

  def extractRelationships(relationshipGraph: RelationshipGraph,person : String): Set[String] = {
    (for {
      rel: Relationship <- relationshipGraph.relationships.toSet
      linkedPerson <- Set(rel.startNode, rel.endNode)

      if (rel.startNode == person || rel.endNode == person)
    } yield (linkedPerson)).diff(Set(person))
  }


  def extractDeg1AndDeg2(relationshipGraph : RelationshipGraph) ={
    allThePeople(relationshipGraph).map{p =>
      val relationshipsDeg1 = extractRelationships(relationshipGraph,p)
      (p,relationshipsDeg1,relationshipsDeg1.flatMap(extractRelationships(relationshipGraph,_)).filter(_ != p).diff(relationshipsDeg1))}
  }

  def extractDeg1AndDeg2Numbers(relationshipGraph : RelationshipGraph) ={
    extractDeg1AndDeg2(relationshipGraph).
      map{case (name,relDeg1,relDeg2) => PersonConnections(name, relDeg1.size, relDeg2.size)}
  }


}