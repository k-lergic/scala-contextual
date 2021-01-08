package org.kalergic.contextual.v0.core

import scala.reflect.runtime.universe._

final case class ContextKey[V] private (name: String, valueTypeTag: TypeTag[V])

object ContextKey {
  def forType[V: TypeTag](name: String): ContextKey[V] = ContextKey(name, typeTag[V])
}
