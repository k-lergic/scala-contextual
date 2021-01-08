package org.kalergic.contextual.v0.core

import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.core.ContextDataMap.ContextDataValue

private[core] class ThreadContext(val dataMap: ContextDataMap = new ContextDataMap, val notifier: ListenerNotifier)
  extends Context {

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

  private[core] def copy(): ThreadContext = new ThreadContext(dataMap = dataMap.copy(), notifier = notifier)
  private[core] def activateForCurrentThread(): Unit = notifyContextUpdate(notifier.notifyPut[Any])
  private[core] def deactivateForCurrentThread(): Unit = clear()

  private[this] def notifyContextUpdate[W: TypeTag](updateFn: ContextKey[W] => W => Unit): Unit =
    dataMap.keys.foreach { key =>
      val untyped: ContextDataValue[_] = dataMap.untypedLookup(key)
      untyped.checkTypeAndInvoke(key, updateFn)
    }

  private[core] def size: Int = dataMap.size
  private[core] def isEmpty: Boolean = dataMap.isEmpty
  private[core] def nonEmpty: Boolean = dataMap.nonEmpty
  private[core] def keys: Iterable[ContextKey[_]] = dataMap.keys
}
