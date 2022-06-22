package net.matlux.mailinator

import net.matlux.mailinator.MailinatorDomain._
import spray.json.DefaultJsonProtocol

trait JsonMarshallers extends DefaultJsonProtocol {


  // formats for unmarshalling and marshalling
  implicit val mailboxCreatedFormat = jsonFormat1(MailboxCreated)
  implicit val mailCreatedFormat = jsonFormat6(Mail)
  implicit val errorMsgFormat = jsonFormat1(ErrorMsg)
  implicit val messageFormat = jsonFormat4(Message)



}
