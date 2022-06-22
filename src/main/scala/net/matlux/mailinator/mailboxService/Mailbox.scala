package net.matlux.mailinator.mailboxService

import akka.actor.{Actor, ActorRef}
import net.matlux.mailinator.MailinatorDomain.{EmailAddress, Mail, MailMetaData, Message, PagedMailsInfo}
import net.matlux.mailinator.mailboxService.MailRouter.{CreateMailbox, GetEmailByIndex, GetEmailByPage, MailNotFound, PostEmail}

import java.time.Instant
import scala.collection.immutable.SortedMap
import scala.math.ceil

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
    case (receiver: ActorRef, GetEmailByPage(_, page, size)) =>
      val mailboxSize   = mails.size
      val startItem     = mailboxSize - page * size
      val numberOfPages = ceil(mailboxSize.toDouble / size.toDouble).toInt
      val nextPage      = if (page < numberOfPages) Some(page + 1) else None
      receiver ! PagedMailsInfo(
        page,
        nextPage,
        numberOfPages,
        mailboxSize,
        mails
          .slice(startItem, startItem + size)
          .values
          .toVector
          .map(mail => MailMetaData(mail.id, mail.datetime, mail.from, mail.to, mail.subject))
          .reverse
      )
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
