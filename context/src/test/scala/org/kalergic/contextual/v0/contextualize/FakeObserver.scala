package org.kalergic.contextual.v0.contextualize

import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.FakeListener

class FakeObserver[V <: Contextualizable: TypeTag] extends ContextObserver[V] {

  // Use the FakeListener to hold information delivered to this observer
  val listener: FakeListener[V] = new FakeListener[V](contextKey)

  override def name: String = "fake-observer"
  override def put(v: V): Unit = listener.put(v)
  override def removed(v: V): Unit = listener.removed(v)
}
