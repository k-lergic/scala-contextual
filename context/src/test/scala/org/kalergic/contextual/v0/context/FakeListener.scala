package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._

object FakeListener {

  type Invocation[V] = (V, Long)

  implicit class InvocationSeqOps[V](inv: Seq[Invocation[V]]) extends AnyRef {
    def values: Seq[V] = inv.map(_._1)
    def threadIds: Seq[Long] = inv.map(_._2)
  }
}

class FakeListener[V: TypeTag](override val contextKey: ContextKey[V]) extends ContextValueListener[V] {
  import FakeListener._

  @volatile var puts: Seq[Invocation[V]] = Seq.empty
  @volatile var removes: Seq[Invocation[V]] = Seq.empty

  override def valueTypeTag: TypeTag[V] = typeTag[V]
  override def name: String = s"FakeListener[${typeOf[V]}]"
  override def put(value: V): Unit = puts = puts :+ ((value, Thread.currentThread().getId))
  override def removed(value: V): Unit = removes = removes :+ ((value, Thread.currentThread().getId))
}
