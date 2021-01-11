package org.kalergic.contextual.v0.contextualize

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe._

import org.kalergic.contextual.v0.context.ContextualizedExecutionContext
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

case class TestId(id: String) extends Contextualizable
case class TestHigherId[A](a: A) extends Contextualizable

class ContextualizeSummonSpec extends AnyFlatSpec with BeforeAndAfterEach with should.Matchers with ScalaFutures {

  implicit val ec: ExecutionContext = ContextualizedExecutionContext.Implicits.global

  override def beforeEach(): Unit = clearContext()
  val testId: TestId = TestId("foo")

  "contextualize and summon" should "work" in {
    contextualize[TestId](testId)
    summon[TestId] should contain(testId)
    assert(summon[TestId].get eq testId)
  }

  they should "work for higher-kinded types" in {
    contextualize[TestHigherId[Int]](TestHigherId(42))
    contextualize[TestHigherId[List[Int]]](TestHigherId(List(1, 2, 3)))
    summon[TestHigherId[Int]] should contain(TestHigherId(42))
    summon[TestHigherId[List[Int]]] should contain(TestHigherId(List(1, 2, 3)))
  }

  "contextualize" should "replace a value for the current thread and for child threads but not in a parent thread" in {
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

  "decontextualize" should "remove a value from context" in {
    contextualize[TestId](testId)
    assume(summon[TestId].contains(testId))
    decontextualize[TestId]()
    summon[TestId] shouldBe empty
  }

  it should "remove a value from context for child threads" in {
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

  "summon" should "summon a value contextualized in a parent thread" in {
    contextualize[TestId](testId)
    @volatile var futureExecuted = false
    Future {
      summon[TestId] should contain(testId)
      assert(summon[TestId].get eq testId)
      futureExecuted = true
    }.futureValue

    assume(futureExecuted)
  }

  it should "return an empty option for a type that has not been contextualized" in {
    summon[TestId] shouldBe empty
  }

  it should "have no effect for a type that has not been contextualized" in {
    assume(summon[TestId].isEmpty)
  }

  "keyFor" should "construct the correct key for a simple type" in {
    val k = keyFor[TestId]
    k.name shouldBe typeOf[TestId].toString
    k.valueTypeTag shouldBe typeTag[TestId]
  }

  it should "construct the correct key for higher-kinded types" in {
    val kInt = keyFor[TestHigherId[Int]]
    val kListInt = keyFor[TestHigherId[List[Int]]]
    kInt.name shouldBe typeOf[TestHigherId[Int]].toString
    kInt.valueTypeTag shouldBe typeTag[TestHigherId[Int]]
    kListInt.name shouldBe typeOf[TestHigherId[List[Int]]].toString
    kListInt.valueTypeTag shouldBe typeTag[TestHigherId[List[Int]]]
  }

  "contextualized (execution context)" should "wrap an execution context" in {
    val ec = scala.concurrent.ExecutionContext.Implicits.global
    val cec = contextualized(ec)
    cec match {
      case _: ContextualizedExecutionContext => // ok
      case x => fail(s"wrong type: $x")
    }
  }

  it should "not re-wrap a ContextualizedExecutionContext" in {
    val ec = scala.concurrent.ExecutionContext.Implicits.global
    contextualized(ec) eq ec
  }

  "startObserving" should "register an observer and allow context changes to be propagated to the observer" in {
    import org.kalergic.contextual.v0.context.FakeListener._
    val fakeObserver = new FakeObserver[TestId]
    startObserving[TestId](fakeObserver)
    contextualize[TestId](testId)
    fakeObserver.listener.puts.values shouldBe Seq(testId)
    fakeObserver.listener.puts.threadIds shouldBe Seq(Thread.currentThread().getId)
    // Underlying implementation tests that the correct threads receive the correct updates.
  }

  it should "only deliver updates to an observer once if the observer is registered more than once" in {
    import org.kalergic.contextual.v0.context.FakeListener._
    val fakeObserver = new FakeObserver[TestId]
    startObserving[TestId](fakeObserver)
    startObserving[TestId](fakeObserver)
    contextualize[TestId](testId)
    fakeObserver.listener.puts.values shouldBe Seq(testId)
    fakeObserver.listener.puts.threadIds shouldBe Seq(Thread.currentThread().getId)
  }

  "stopObserving" should "de-register an observer and stop context changes from being propagated to the observer" in {
    import org.kalergic.contextual.v0.context.FakeListener._
    val fakeObserver = new FakeObserver[TestId]
    startObserving[TestId](fakeObserver)
    contextualize[TestId](testId)

    stopObserving[TestId](fakeObserver)

    decontextualize[TestId]()

    val anotherTestId = TestId("another")
    contextualize[TestId](anotherTestId)

    fakeObserver.listener.puts.values shouldBe Seq(testId)
    fakeObserver.listener.puts.threadIds shouldBe Seq(Thread.currentThread().getId)
    fakeObserver.listener.removes.values shouldBe Seq.empty
    fakeObserver.listener.removes.threadIds shouldBe Seq.empty
  }

  it should "have no effect on other registered observers" in {
    import org.kalergic.contextual.v0.context.FakeListener._

    val fakeObserver = new FakeObserver[TestId]
    val anotherFakeObserver = new FakeObserver[TestId]
    startObserving[TestId](fakeObserver)
    startObserving[TestId](anotherFakeObserver)
    contextualize[TestId](testId)

    stopObserving[TestId](fakeObserver)
    decontextualize[TestId]()

    val anotherTestId = TestId("another")
    contextualize[TestId](anotherTestId)

    val tid = Thread.currentThread().getId

    fakeObserver.listener.puts.values shouldBe Seq(testId)
    fakeObserver.listener.puts.threadIds shouldBe Seq(Thread.currentThread().getId)
    fakeObserver.listener.removes.values shouldBe Seq.empty
    fakeObserver.listener.removes.threadIds shouldBe Seq.empty

    anotherFakeObserver.listener.puts.values shouldBe Seq(testId, anotherTestId)
    anotherFakeObserver.listener.puts.threadIds shouldBe Seq(tid, tid)
    anotherFakeObserver.listener.removes.values shouldBe Seq(testId)
    anotherFakeObserver.listener.removes.threadIds shouldBe Seq(tid)
  }
}
