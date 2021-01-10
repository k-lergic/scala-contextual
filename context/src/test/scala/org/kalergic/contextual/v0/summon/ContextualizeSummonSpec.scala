package org.kalergic.contextual.v0.summon

import scala.concurrent.{ExecutionContext, Future}

import org.kalergic.contextual.v0.context.ContextualizedExecutionContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

case class TestId(id: String) extends Summonable[TestId]

class ContextualizeSummonSpec extends AnyFlatSpec with should.Matchers with ScalaFutures {

  class ContextualizeSummonFixture {
    implicit val ec: ExecutionContext = ContextualizedExecutionContext.Implicits.global
    val testId: TestId = TestId("foo")
    contextualize[TestId](testId)
  }

  "contextualize and summon" should "work" in new ContextualizeSummonFixture {
    summon[TestId] should contain(testId)
    assert(summon[TestId].get eq testId)
  }

  "summon" should "summon a value contextualized in a parent thread" in new ContextualizeSummonFixture {

    @volatile var futureExecuted = false
    Future {
      summon[TestId] should contain(testId)
      assert(summon[TestId].get eq testId)
      futureExecuted = true
    }.futureValue

    assume(futureExecuted)
  }

  "decontextualize" should "remove a value from context" in new ContextualizeSummonFixture {
    assume(summon[TestId].contains(testId))
    decontextualize[TestId]()
    summon[TestId] shouldBe empty
  }

  "decontextualize" should "remove a value from context for child threads" in new ContextualizeSummonFixture {

    @volatile var outerFutureExecuted = false
    @volatile var innerFutureExecuted = false
    Future {
      assume(summon[TestId].contains(testId))
      decontextualize[TestId]()
      Future {
        summon[TestId] shouldBe empty
        innerFutureExecuted = true
      }.futureValue
      outerFutureExecuted = true
    }.futureValue

    assume(outerFutureExecuted)
    assume(innerFutureExecuted)
  }

  "contextualize" should "replace a value for the current thread and for child threads but not in a parent thread" in new ContextualizeSummonFixture {

    val originalTestId: TestId = testId

    @volatile var outerFutureExecuted = false
    @volatile var innerFutureExecuted = false
    Future {

      assume(summon[TestId].contains(originalTestId))

      val anotherTestId: TestId = TestId("another")
      contextualize[TestId](anotherTestId)

      Future {
        summon[TestId] should contain(anotherTestId)
        innerFutureExecuted = true
      }.futureValue
      outerFutureExecuted = true
    }.futureValue

    assume(outerFutureExecuted)
    assume(innerFutureExecuted)
    summon[TestId] should contain(originalTestId)
  }
}
