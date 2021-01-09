package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._
import scala.collection.mutable

trait FakeContext extends Context {

  @volatile var dataMap: mutable.Map[ContextKey[_], Any] = mutable.Map.empty

  @volatile var putCallCount: Int = 0
  @volatile var getCallCount: Int = 0
  @volatile var removeCallCount: Int = 0
  @volatile var clearCallCount: Int = 0

  override def put[V: TypeTag](contextKey: ContextKey[V], value: V): Option[V] = {
    putCallCount += 1
    dataMap.put(contextKey, value).map(_.asInstanceOf[V])
  }

  override def get[V](contextKey: ContextKey[V]): Option[V] = {
    getCallCount += 1
    dataMap.get(contextKey).map(_.asInstanceOf[V])
  }

  override def remove[V: TypeTag](contextKey: ContextKey[V]): Option[V] = {
    removeCallCount += 1
    dataMap.remove(contextKey).map(_.asInstanceOf[V])
  }

  override def clear(): Unit = {
    clearCallCount += 1
    dataMap.clear()
  }
}
