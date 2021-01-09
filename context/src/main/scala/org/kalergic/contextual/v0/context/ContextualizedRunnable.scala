package org.kalergic.contextual.v0.context

private[context] final class ContextualizedRunnable(val target: Runnable, threadLocalContext: ThreadLocalContext)
  extends Runnable {

  private[this] val snapshot: Option[ShareableContext] = threadLocalContext.snapshot

  override def run(): Unit = {
    val maybeOldContext = threadLocalContext.snapshot
    threadLocalContext.setContext(snapshot)
    try {
      target.run()
    } finally {
      threadLocalContext.setContext(maybeOldContext)
    }
  }
}
