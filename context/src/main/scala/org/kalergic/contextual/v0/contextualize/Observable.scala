package org.kalergic.contextual.v0.contextualize

import scala.reflect.runtime.universe._

abstract class Observable[V: TypeTag] extends Summonable[V]
