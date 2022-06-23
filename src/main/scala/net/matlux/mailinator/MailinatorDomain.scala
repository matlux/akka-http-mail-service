package net.matlux.mailinator
import java.time.Instant


import scala.collection.Set
import scala.collection.immutable.SortedMap

object MailinatorDomain {


  // domain model
  type EmailAddress = String
  final case class MailboxCreated(emailAddress: String)
  final case class Message(from: String, to: String, subject: String, content : String)
  final case class Mail(id :Int, datetime : Long, from: String, to: String, subject: String, content : String)
  final case class MailMetaData(id :Int, datetime : Long, from: String, to: String, subject: String)
  final case class PagedMailsInfo(page: Int,
                                  nextPage: Option[Int],
                                  numberOfPages : Int,
                                  numberOfMails : Int,
                                  mails : Vector[MailMetaData])

  // error messages
  final case class ErrorMsg(message: String)

}
