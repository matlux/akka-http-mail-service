package net.matlux.mailinator.mailboxService

import akka.actor.{Actor, ActorRef}
import net.matlux.mailinator.MailinatorDomain.{EmailAddress, Mail, Message}
import net.matlux.mailinator.mailboxService.MailRouter.{CreateMailbox, GetEmailByIndex, MailNotFound, PostEmail}

import java.time.Instant
import scala.collection.immutable.SortedMap

class Mailbox extends Actor{




  type MailboxStorage = SortedMap[Int,Mail]

  override def receive: Receive = receive(SortedMap.empty);

  def receive(mails: MailboxStorage): Receive = {
    case (restEndPoint : ActorRef, PostEmail(_, msg: Message)) => {
      val newId = if (mails.isEmpty) 1 else mails.lastKey + 1
      val mail = Mail(newId,
                      System.currentTimeMillis(),
                      msg.from, msg.to, msg.subject, msg.content)
      context.become(receive(mails + (newId -> mail)))
      restEndPoint ! mail
    }
    case (restEndPoint : ActorRef, GetEmailByIndex(_, id: Int)) =>
      mails.get(id) match {
        case Some(mail) =>
          restEndPoint ! mail
        case None =>
          restEndPoint ! MailNotFound
      }
//    {
//
//      val mail = Mail(newId,
//        System.currentTimeMillis(),
//        msg.from, msg.to, msg.subject, msg.content)
//      context.become(receive(mails + (newId -> mail)))
//      restEndPoint ! mail
//    }
  }

}
