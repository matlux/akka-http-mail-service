package net.matlux.thirdparty

import net.matlux.mailinator.MailinatorDomain.{Person, Relationship, RelationshipGraph}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait ThirdPartyService {
  def getSocialNetwork(socialNetwork : String) : Future[RelationshipGraph] = socialNetwork match {
    case "facebook" => Future(RelationshipGraph("facebook",
                        List(Person("John"), Person("Harry"), Person("Peter"), Person("George"), Person("Anna")),
                        List(Relationship("HasConnection","John","Peter"),
                          Relationship("HasConnection","John","George"),
                          Relationship("HasConnection","Peter","Anna"))))
    case "twitter" => Future(RelationshipGraph("facebook",
      List(Person("John"), Person("Harry"), Person("Peter"), Person("George"), Person("Anna")),
      List(Relationship("HasConnection","John","Peter"),
        Relationship("HasConnection","John","George"),
        Relationship("HasConnection","Peter","George"))))
    case default => Future{throw new RuntimeException("boom!")}


  }
}

object ThirdPartyService {
  def getInstance() = {
    new ThirdPartyService() {

    }
  }

}
