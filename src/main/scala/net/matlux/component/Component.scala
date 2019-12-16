package net.matlux.component

import cats.Eval
import cats.effect.IO

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

object Component {

  import ExecutionContext.Implicits.global

  implicit class FunctionLogging[T](b: => T) extends Logging {
    def log(description: String, operation: String) = {
      logger.info(s"$description ${operation.toUpperCase()}...")
      try {
        val c = b
        logger.info(s"$description ${operation.toUpperCase()} SUCCESS")
        c
      } catch {
        case NonFatal(e) =>
          logger.warn(s"$description ${operation.toUpperCase()} FAILED  - ${e.getMessage}")
          throw e
      }
    }
  }

  type Safe[T] = fs2.Stream[IO, T]

  /**
   * Makes sure the component initialisation is safe (doesn't throw) and that finalisation is always done.
   * In order to compose components please use flatMap or for-comprehension. i.e.
   * {{{
   * for {
   *   c1 <- safe(initc1)(closec1)
   *   c2 <- safe(initc2)(closec2)
   *   c3 <- safe(initc3(c1,c2))(closec3)
   * } yield c3
   * }}}
   *
   * To run such a component standalone please use the run method, i.e.
   * {{{
   * run(safe(initc1)(closec1)).flatMap( closable => system.addShutdownHook( closable.close()) )
   * }}}
   * @param init  - creates the component
   * @param close - finalises the component
   * @tparam T - component type
   * @return safe version of component
   */
  def safe[T: ClassTag](init: => T, description: String = "")(close: => T => Unit): Safe[T] = {
    val desc = if (description == "") implicitly[ClassTag[T]].runtimeClass.getSimpleName else description
    fs2.Stream.bracket(IO(init log (desc, "START")))(x => fs2.Stream.eval(IO(x)), x => IO(close(x) log (desc, "CLOSE")))
  }

  /**
   * Runs a safe component and waits until it is closed. Once the close method is called it will start the finalization of the component and it's nested components.
   *
   * @param component
   * @tparam T
   * @return
   */
//  def run[T](component: Safe[T]): Future[Closable] = get(component).map(_._1)
//
//  def get[T](component: Safe[T]): Future[(Closable, T)] = {
//    val started = Promise[(Closable, T)]()
//    val shutdown = Promise[Unit]()
//    new Closable {
////      val running: Future[Unit] = component.evalMap { c => started.trySuccess((this, c)); IO.fromFuture(IO { shutdown.future }) }.run.unsafeToFuture()
//      val running: Future[Unit] = component.evalMap { c => started.trySuccess((this, c)); IO.fromFuture(Eval.always{ shutdown.future }) }.run.unsafeToFuture()
//
////      running.onFailure { case e => started.tryFailure(e) }
//
//      override def close(): Future[Unit] = {
//        shutdown.trySuccess(())
////        running
//      }
//    }
//    started.future
//  }

}
