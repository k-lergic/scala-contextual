package org.kalergic.contextual.v0.contextualize

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.TypeTag

abstract class Contextualizable[V: TypeTag] {
  def typeTag: TypeTag[V] = ru.typeTag[V]
}
