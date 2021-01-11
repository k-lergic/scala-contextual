package org.kalergic.contextual.v0.correlation

import com.typesafe.scalalogging.StrictLogging
import org.kalergic.contextual.v0.contextualize.ContextObserver
import org.slf4j.MDC

private[correlation] class MDCCorrelationIdObserver extends ContextObserver[CorrelationId] with StrictLogging {
  import MDCCorrelationIdObserver._
  override def name: String = s"$MDCCorrelationIdKey.MDCObserver"
  override def put(value: CorrelationId): Unit =
    MDC.put(MDCCorrelationIdKey, value.id)
  override def removed(value: CorrelationId): Unit =
    MDC.remove(MDCCorrelationIdKey)
}

private[correlation] object MDCCorrelationIdObserver {
  private[correlation] val MDCCorrelationIdKey = "contextual.correlation.correlationId"
}
