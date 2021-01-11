package org.kalergic.contextual.v0

import scala.concurrent.ExecutionContext
import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.{ContextKey, Contextual}

package object contextualize {

  private[this] val contextual: Contextual = Contextual()

  def contextualized(ec: ExecutionContext): ExecutionContext = {
    import org.kalergic.contextual.v0.context.ContextualizedExecutionContext.Implicits._
    ec.contextualized
  }

  def contextualize[V <: Contextualizable: TypeTag](v: V): Unit = contextual.put(keyFor[V], v)
  def decontextualize[V <: Contextualizable: TypeTag](): Unit = contextual.remove[V](keyFor[V])
  def clearContext(): Unit = contextual.clear()

  def summon[V <: Contextualizable: TypeTag]: Option[V] = contextual.get(keyFor[V])

  def startObserving[V <: Contextualizable](observer: ContextObserver[V]): Unit = contextual.addListener(observer)
  def stopObserving[V <: Contextualizable](observer: ContextObserver[V]): Unit = contextual.removeListener(observer)

  private[this] def keyNameFor[V <: Contextualizable: TypeTag]: String = typeOf[V].toString

  private[contextualize] def keyFor[V <: Contextualizable: TypeTag]: ContextKey[V] =
    ContextKey.forType[V](keyNameFor[V])
}
