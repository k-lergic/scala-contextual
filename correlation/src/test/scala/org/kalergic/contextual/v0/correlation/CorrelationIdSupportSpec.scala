package org.kalergic.contextual.v0.correlation

import org.kalergic.contextual.v0.contextualize._
import org.kalergic.contextual.v0.correlation.MDCCorrelationIdObserver.MDCCorrelationIdKey
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.slf4j.MDC

class CorrelationIdSupportSpec extends AnyFlatSpec with BeforeAndAfterEach with should.Matchers {

  override def beforeEach(): Unit = clearContext()

  "CorrelationIdSupport" should "register an observer set the correlation id in MDC when it is contextualized" in {
    CorrelationIdLogging.install()

    val correlationId = CorrelationId("foo")
    contextualize[CorrelationId](correlationId)

    MDC.get(MDCCorrelationIdKey) shouldBe correlationId.id
  }

  it should "overwrite a correlation id in MDC when a new one is contextualized" in {
    CorrelationIdLogging.install()

    val correlationId = CorrelationId("foo")
    contextualize[CorrelationId](correlationId)

    assume(MDC.get(MDCCorrelationIdKey) === correlationId.id)

    val anotherCorrelationId = CorrelationId("bar")
    contextualize[CorrelationId](anotherCorrelationId)

    MDC.get(MDCCorrelationIdKey) shouldBe anotherCorrelationId.id
  }

  it should "remove a correlation id in MDC when the correlation id is decontextualized" in {
    CorrelationIdLogging.install()

    val correlationId = CorrelationId("foo")
    contextualize[CorrelationId](correlationId)

    assume(MDC.get(MDCCorrelationIdKey) === correlationId.id)

    decontextualize[CorrelationId]()

    MDC.get(MDCCorrelationIdKey) shouldBe null
  }
}
