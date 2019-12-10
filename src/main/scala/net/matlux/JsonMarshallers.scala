package net.matlux

import net.matlux.SocialNetworkDomain.{Person, PersonConnections, Relationship, RelationshipGraph}
import spray.json.DefaultJsonProtocol

trait JsonMarshallers extends DefaultJsonProtocol {


  // formats for unmarshalling and marshalling
  implicit val relationshipFormat = jsonFormat3(Relationship)
  implicit val personFormat = jsonFormat1(Person)
  implicit val relationshipGraphFormat = jsonFormat3(RelationshipGraph)
  implicit val personConnections = jsonFormat3(PersonConnections)

}
