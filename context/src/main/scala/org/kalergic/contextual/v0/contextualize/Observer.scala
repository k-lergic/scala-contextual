package org.kalergic.contextual.v0.contextualize

import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.{ContextKey, ContextValueListener}

abstract class Observer[V <: Summonable[V]: TypeTag] extends ContextValueListener[V] {
  final val valueTypeTag: TypeTag[V] = typeTag[V]
  final val contextKey: ContextKey[V] = keyFor[V]()
  final def name: String = ""
}
