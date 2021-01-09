package org.kalergic.contextual.v0.context

import scala.concurrent.ExecutionContext

final class ContextualizedExecutionContext private[context] (target: ExecutionContext) extends ExecutionContext {

  override def execute(runnable: Runnable): Unit = runnable match {
    case cr: ContextualizedRunnable => target.execute(cr)
    case _ => target.execute(new ContextualizedRunnable(runnable, GlobalContext))
  }

  override def reportFailure(cause: Throwable): Unit = target.reportFailure(cause)
}

object ContextualizedExecutionContext {

  object Implicits {

    implicit val global: ExecutionContext = ExecutionContext.Implicits.global.contextualized

    implicit class ContextPassingDecoration(ec: ExecutionContext) extends AnyRef {

      def contextualized: ContextualizedExecutionContext =
        ec match {
          case cec: ContextualizedExecutionContext => cec
          case _ => new ContextualizedExecutionContext(ec)
        }
    }
  }
}
