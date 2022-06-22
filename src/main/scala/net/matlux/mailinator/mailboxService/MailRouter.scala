package net.matlux.mailinator.mailboxService

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.matlux.mailinator.MailinatorDomain.{EmailAddress, MailboxCreated}
import net.matlux.mailinator.mailboxService.MailRouter.{CreateMailbox, Mailboxes}

import java.util.UUID

object MailRouter {
  final val Name = "MailRouter"

  final case class CreateMailbox(emailAddress: String)
  type Mailboxes = Map[EmailAddress,ActorRef]
}

class MailRouter extends Actor {


  val log = Logging(context.system, this)

  override def receive: Receive = receive(Map.empty);

  def receive(mailStorage: Mailboxes): Receive = {
    case CreateMailbox(emailAddress) => {

      val mailboxActor: ActorRef = context.actorOf(Props[Mailbox], s"mailbox-$emailAddress")
      context.become(receive(mailStorage + (emailAddress -> mailboxActor)))
      log.debug("MailboxCreated({})", emailAddress)
      sender() ! MailboxCreated(emailAddress)
    }
  }
}
