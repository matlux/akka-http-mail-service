package net.matlux.mailinator

import MailinatorDomain._
import net.matlux.utils.Atom

import java.util.UUID
import scala.collection.immutable.SortedMap;

object MailboxService {

  def createEmptyMailbox(): Mailbox = {
    SortedMap.empty[Int, Message]
  }

  def createEmailAddress(ctx: MailStorageCtx): EmailAddress = {
    val emailAddress = s"${UUID.randomUUID().toString}@test.com"
    ctx.swap((mailStorage) => mailStorage + (emailAddress -> createEmptyMailbox()))
    emailAddress
  }
  def postNewMessage(ctx: MailStorageCtx, emailAddress: EmailAddress, message : Message): EmailAddress = {
    val emailAddress = s"${UUID.randomUUID().toString}@test.com"
    ctx.swap((mailStorage) => {
      val mailbox = mailStorage.get(emailAddress).getOrElse();
      val newId = if (mailbox.isEmpty) 1 else mailbox.lastKey + 1

      mailStorage + (emailAddress -> createEmptyMailbox())})
    emailAddress
  }

}

trait MailStorageCtx extends Atom[MailStorage] {}
