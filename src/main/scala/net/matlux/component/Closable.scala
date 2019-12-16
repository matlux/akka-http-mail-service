package net.matlux.component

import scala.concurrent.{ExecutionContext, Future}

trait Closable {
  def close(): Future[Unit]
}

object Closable {
  implicit def toClosable[T](f: => Future[T])(implicit ec: ExecutionContext): Closable = new Closable {
    override def close(): Future[Unit] = f.map(_ => ())
  }

  def all(c: Closable*)(implicit ec: ExecutionContext): Closable = new Closable {
    override def close(): Future[Unit] = Future.sequence(c.map(_.close())).map(x => ())
  }
}
