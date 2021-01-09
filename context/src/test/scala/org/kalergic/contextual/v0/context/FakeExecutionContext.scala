package org.kalergic.contextual.v0.context

import scala.concurrent.ExecutionContext

private[context] final class FakeExecutionContext extends ExecutionContext {

  @volatile var runnables: Seq[ContextualizedRunnable] = Seq.empty
  @volatile var throwables: Seq[Throwable] = Seq.empty

  override def execute(r: Runnable): Unit =
    r match {
      case cr: ContextualizedRunnable => runnables :+= cr
      case _ => throw new Exception("The Runnable should be an instance of ContextualizedRunnable")
    }
  override def reportFailure(th: Throwable): Unit =
    throwables = throwables :+ th
}
