package org.kalergic.contextual.v0

import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.{ContextKey, Contextual}

package object summon {

  private[this] val contextual: Contextual = Contextual()

  def contextualize[V <: Summonable[V]: TypeTag](v: V): Unit = contextual.put(keyFor[V], v)
  def decontextualize[V <: Summonable[V]: TypeTag](): Unit = contextual.remove[V](keyFor[V])
  def summon[V <: Summonable[V]: TypeTag]: Option[V] = contextual.get(keyFor[V])
  def clearContextualizedData(): Unit = contextual.clear()

  private[this] def keyFor[V: TypeTag] = ContextKey.forType[V](s"contextual.summonable.${typeOf[V]}")
}
