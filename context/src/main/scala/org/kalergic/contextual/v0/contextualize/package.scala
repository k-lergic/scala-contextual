package org.kalergic.contextual.v0

import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.{ContextKey, Contextual}

package object contextualize {

  private[this] val contextual: Contextual = Contextual()

  def contextualize[V <: Summonable[V]: TypeTag](v: V): Unit = contextual.put(keyFor[V](), v)
  def decontextualize[V <: Summonable[V]: TypeTag](): Unit = contextual.remove[V](keyFor[V]())
  def clearContextualizedData(): Unit = contextual.clear()

  def summon[V <: Summonable[V]: TypeTag]: Option[V] = contextual.get(keyFor[V]())

  def observe[V <: Observable[V]](observer: Observer[V]): Unit = contextual.addListener(observer)
  def stopObserving[V <: Observable[V]](observer: Observer[V]): Unit = contextual.removeListener(observer)

  private[contextualize] def keyNameFor[V: TypeTag](prefix: Option[String] = None): String =
    s"contextual.summonable.${prefix.map(p => s"$p.").getOrElse("")}.${typeOf[V]}"
  private[contextualize] def keyFor[V: TypeTag](prefix: Option[String] = None): ContextKey[V] =
    ContextKey.forType[V](keyNameFor[V](prefix))
}
