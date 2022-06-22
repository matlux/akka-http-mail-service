package net.matlux.mailinator.mailboxService

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.matlux.mailinator.MailinatorDomain.{EmailAddress, MailboxCreated, Message}
import net.matlux.mailinator.mailboxService.MailRouter.{CreateMailbox, GetEmailByIndex, GetEmailByPage, MailboxNotFound, Mailboxes, PostEmail}

import java.util.UUID

object MailRouter {
  final val Name = "MailRouter"

  // interface
  final case class CreateMailbox(emailAddress: String)
  final case class PostEmail(emailAddress: String, msg : Message) // return Mail or MailboxNotFound
  final case class GetEmailByPage(emailAddress : EmailAddress, page : Int, size: Int) // return PagedMailMetaData
  final case class GetEmailByIndex(emailAddress : EmailAddress, id : Int) //
  final case class DeleteMailbox(emailAddress : EmailAddress)

  final case object MailboxNotFound
  final case object MailboxDeleted
  final case object MailDeleted
  final case object MailNotFound

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
    case msg @ PostEmail(emailAddress, _) =>
      mailStorage.get(emailAddress) match {
        case Some(mailboxActor) =>
          mailboxActor ! (sender(), msg)
        case None =>
          sender() ! MailboxNotFound
      }


    case msg @ GetEmailByIndex(emailAddress, _) =>
      mailStorage.get(emailAddress) match {
        case Some(mailbox) =>
          mailbox ! (sender(), msg)
        case None =>
          sender() ! MailboxNotFound
      }

    case msg @ GetEmailByPage(emailAddress, _, _) =>
      mailStorage.get(emailAddress) match {
        case Some(mailbox) =>
          mailbox ! (sender(), msg)
        case None =>
          sender() ! MailboxNotFound
      }
  }
}
