package org.kalergic.contextual.examples.futures

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.{ExecutionContext, Future}

import com.typesafe.scalalogging.StrictLogging
import org.kalergic.contextual.v0.contextualize._
import org.kalergic.contextual.v0.correlation.{CorrelationId, CorrelationIdMDCSupport}

object FuturesExample extends App with StrictLogging {

  // Decorate the execution context
  implicit val ec: ExecutionContext = contextualized(Implicits.global)

  // Install support for correlation ids
  CorrelationIdMDCSupport.install()

  // Somewhere else ...

  val correlationId = CorrelationId(UUID.randomUUID().toString)
  contextualize(correlationId)

  // Somewhere else ...

  logger.info(s"Summoned in original thread: ${summon[CorrelationId]}")

  Future {
    // No change to method signature to include correlation id!
    SomeOtherCode.execute()
  }

  Thread.sleep(600000)
}

object SomeOtherCode extends StrictLogging {

  def execute(): Unit = {
    // We can summon the correlation id!
    val summoned = summon[CorrelationId]

    logger.info(s"Summoned in delegated thread: $summoned")

    // And it will show up in the log (provided we configured logging correctly to use MDC data)!
    logger.info("Some other message")
  }
}
