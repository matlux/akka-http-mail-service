package net.matlux.mailinator.mailboxService

import akka.actor.Actor
import net.matlux.mailinator.MailinatorDomain.{EmailAddress, Message}
import net.matlux.mailinator.mailboxService.MailRouter.CreateMailbox

import scala.collection.immutable.SortedMap

class Mailbox extends Actor{


  case class PostEmail(msg : Message)

  type MailboxStorage = SortedMap[Int,Message]

  override def receive: Receive = receive(SortedMap.empty);

  def receive(mailbox: MailboxStorage): Receive = {
    case PostEmail(msg: Message) => {

    }
  }

}
