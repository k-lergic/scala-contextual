package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._

object Contextual {
  def apply(): Contextual = new Contextual(backing = GlobalContext)
}

final class Contextual private[context] (private[context] val backing: Context with ListenerManager)
  extends Context
  with ListenerManager {

  override def put[V: TypeTag](key: ContextKey[V], value: V): Option[V] = backing.put(key, value)

  override def get[V](key: ContextKey[V]): Option[V] = backing.get(key)

  override def remove[V: TypeTag](key: ContextKey[V]): Option[V] = backing.remove(key)

  override def clear(): Unit = backing.clear()

  override def addListener[V](listener: ContextValueListener[V]): Unit = backing.addListener(listener)

  override def removeListener[V](listener: ContextValueListener[V]): Unit = backing.removeListener(listener)
}
