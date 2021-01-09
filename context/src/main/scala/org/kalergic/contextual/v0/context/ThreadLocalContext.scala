package org.kalergic.contextual.v0.context

private[context] trait ThreadLocalContext { self: Context with ListenerManager =>

  private[context] val threadLocalContext: ThreadLocal[Option[ShareableContext]] = ThreadLocal.withInitial(() => None)
  private[context] def currentThreadContext: Option[ShareableContext] = threadLocalContext.get()

  private[context] def snapshot: Option[ShareableContext] = currentThreadContext.map(_.copy())

  private[context] def set(context: Option[ShareableContext]): Unit = {
    currentThreadContext.foreach(_.deactivateForCurrentThread())
    threadLocalContext.set(context)
    context.foreach(_.activateForCurrentThread())
  }
}
