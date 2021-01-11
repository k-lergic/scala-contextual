package org.kalergic.contextual.v0.contextualize

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.TypeTag

abstract class Summonable[V: TypeTag] {
  def typeTag: TypeTag[V] = ru.typeTag[V]
}
