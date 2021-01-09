package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.ContextDataMap.ContextDataValue

private[context] class ThreadContext(
  private[context] val dataMap: ContextDataMap = new ContextDataMap,
  private[context] val notifier: ListenerNotifier
) extends ShareableContext {

  override def put[V: TypeTag](key: ContextKey[V], value: V): Option[V] = {
    val old = dataMap.put(key, ContextDataValue(value))
    notifier.notifyPut(key)(value)
    old.map(_.value)
  }

  override def get[V](key: ContextKey[V]): Option[V] = dataMap.get(key).map(_.value)

  override def remove[V: TypeTag](key: ContextKey[V]): Option[V] = {
    val removed = dataMap.remove(key).map(_.value)
    removed.foreach(notifier.notifyRemove(key)(_))
    removed
  }

  override def clear(): Unit = {
    notifyCleared()
    dataMap.clear()
  }

  private[this] def notifyCleared(): Unit = notifyContextUpdate(notifier.notifyRemove[Any])

  private[context] def notifyContextUpdate[W: TypeTag](updateFn: ContextKey[W] => W => Unit): Unit =
    dataMap.keys.foreach { key =>
      val untyped: ContextDataValue[_] = dataMap.untypedLookup(key)
      untyped.checkTypeAndInvoke(key, updateFn)
    }

  private[context] def size: Int = dataMap.size
  private[context] def isEmpty: Boolean = dataMap.isEmpty
  private[context] def nonEmpty: Boolean = dataMap.nonEmpty
  private[context] def keys: Iterable[ContextKey[_]] = dataMap.keys

  override private[context] def copy(): ThreadContext = new ThreadContext(dataMap = dataMap.copy(), notifier = notifier)
  override private[context] def activateForCurrentThread(): Unit = notifyContextUpdate(notifier.notifyPut[Any])
  override private[context] def deactivateForCurrentThread(): Unit = clear()

}
