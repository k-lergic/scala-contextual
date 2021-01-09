package org.kalergic.contextual.v0.core

private[core] trait ThreadLocalContext { self: Context with ListenerManager =>

  private[core] val threadLocalContext: ThreadLocal[Option[ShareableContext]] = ThreadLocal.withInitial(() => None)
  private[this] def currentThreadContext: Option[ShareableContext] = threadLocalContext.get()

  private[core] def snapshot: Option[ShareableContext] = currentThreadContext.map(_.copy())

  private[core] def set(context: Option[ShareableContext]): Unit = {
    currentThreadContext.foreach(_.deactivateForCurrentThread())
    threadLocalContext.set(context)
    context.foreach(_.activateForCurrentThread())
  }
}
