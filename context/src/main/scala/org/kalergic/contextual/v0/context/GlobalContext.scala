package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._

private[context] object GlobalContext extends ListenerManagerImpl with Context with ThreadLocalContext {

  override def put[V: TypeTag](key: ContextKey[V], value: V): Option[V] = {
    val context: ShareableContext = currentThreadContext match {
      case Some(ctx) => ctx
      case None =>
        val ctx = new ThreadContext(notifier = this)
        setContext(Some(ctx))
        ctx
    }
    context.put(key, value)
  }

  override def get[V](key: ContextKey[V]): Option[V] = currentThreadContext.flatMap(_.get(key))

  override def remove[V: TypeTag](key: ContextKey[V]): Option[V] = {
    val result = currentThreadContext.flatMap(_.remove(key))
    if (currentThreadContext.exists(_.isEmpty)) {
      threadLocalContext.set(None)
    }
    result
  }

  override def clear(): Unit = {
    currentThreadContext.foreach(_.clear())
    threadLocalContext.set(None)
  }
}
