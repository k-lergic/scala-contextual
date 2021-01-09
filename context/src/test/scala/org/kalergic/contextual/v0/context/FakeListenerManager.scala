package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._

private[context] object FakeListenerManager {
  type KVPair[V] = (ContextKey[V], V)

  implicit class KVPairSeqOps(kvs: Seq[KVPair[_]]) {
    def keys: Seq[ContextKey[_]] = kvs.map(_._1)
    def values: Seq[_] = kvs.map(_._2)
  }
}

private[context] trait FakeListenerManager extends ListenerManagerImpl {
  import FakeListenerManager._

  @volatile var puts: Seq[KVPair[_]] = Seq.empty
  @volatile var removes: Seq[KVPair[_]] = Seq.empty

  @volatile var addListenerCallCount: Int = 0
  @volatile var removeListenerCallCount: Int = 0

  override abstract def notifyPut[V: TypeTag](key: ContextKey[V])(value: V): Unit =
    puts = puts :+ ((key, value))

  override abstract def notifyRemove[V: TypeTag](key: ContextKey[V])(value: V): Unit =
    removes = removes :+ ((key, value))

  override def addListener[V](listener: ContextValueListener[V]): Unit = {
    addListenerCallCount += 1
    super.addListener(listener)
  }

  override def removeListener[V](listener: ContextValueListener[V]): Unit = {
    removeListenerCallCount += 1
    super.removeListener(listener)
  }
}
