package org.kalergic.contextual.v0.summon

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.ContextualizedExecutionContext
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

case class TestId(id: String) extends Summonable[TestId]
case class TestHigherId[A: TypeTag](a: A) extends Summonable[TestHigherId[A]]

class ContextualizeSummonSpec extends AnyFlatSpec with BeforeAndAfterEach with should.Matchers with ScalaFutures {

  override def beforeEach(): Unit = clearContextualizedData()

  class ContextualizeSummonFixture {
    implicit val ec: ExecutionContext = ContextualizedExecutionContext.Implicits.global
    val testId: TestId = TestId("foo")
  }

  "contextualize and summon" should "work" in new ContextualizeSummonFixture {
    contextualize[TestId](testId)
    summon[TestId] should contain(testId)
    assert(summon[TestId].get eq testId)
  }

  "summon" should "return an empty option for a type that has not been contextualized" in new ContextualizeSummonFixture {
    summon[TestId] shouldBe empty
  }

  "decontextualize" should "have no effect for a type that has not bee ncontextualized" in new ContextualizeSummonFixture {
    assume(summon[TestId].isEmpty)
  }

  "contextualize and summon" should "work for higher-kinded types" in new ContextualizeSummonFixture {
    contextualize[TestHigherId[Int]](TestHigherId(42))
    contextualize[TestHigherId[List[Int]]](TestHigherId(List(1, 2, 3)))
    summon[TestHigherId[Int]] should contain(TestHigherId(42))
    summon[TestHigherId[List[Int]]] should contain(TestHigherId(List(1, 2, 3)))
  }

  "summon" should "summon a value contextualized in a parent thread" in new ContextualizeSummonFixture {
    contextualize[TestId](testId)
    @volatile var futureExecuted = false
    Future {
      summon[TestId] should contain(testId)
      assert(summon[TestId].get eq testId)
      futureExecuted = true
    }.futureValue

    assume(futureExecuted)
  }

  "decontextualize" should "remove a value from context" in new ContextualizeSummonFixture {
    contextualize[TestId](testId)
    assume(summon[TestId].contains(testId))
    decontextualize[TestId]()
    summon[TestId] shouldBe empty
  }

  "decontextualize" should "remove a value from context for child threads" in new ContextualizeSummonFixture {
    contextualize[TestId](testId)
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
    contextualize[TestId](testId)
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
