package org.kalergic.contextual.examples.futures

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.{ExecutionContext, Future}

import com.typesafe.scalalogging.StrictLogging
import org.kalergic.contextual.v0.contextualize._
import org.kalergic.contextual.v0.correlation.{CorrelationId, CorrelationIdSupport}

object FuturesExample extends App with StrictLogging {

  implicit val ec: ExecutionContext = contextualized(Implicits.global)
  CorrelationIdSupport.install()

  val correlationId = CorrelationId(UUID.randomUUID().toString)
  contextualize(correlationId)

  logger.info(s"Summoned in original thread: ${summon[CorrelationId]}")

  Future {
    SomeOtherCode.execute()
  }

  Thread.sleep(600000)
}

object SomeOtherCode extends StrictLogging {

  def execute(): Unit = {
    val summoned = summon[CorrelationId]
    logger.info(s"Summoned in delegated thread: $summoned")
    logger.info("Some other message")
  }
}
