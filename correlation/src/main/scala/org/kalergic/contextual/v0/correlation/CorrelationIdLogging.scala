package org.kalergic.contextual.v0.correlation

import org.kalergic.contextual.v0.contextualize._

object CorrelationIdLogging {
  private[this] val mdcObserver: MDCCorrelationIdObserver = new MDCCorrelationIdObserver
  def install(): Unit = startObserving[CorrelationId](mdcObserver)
}
