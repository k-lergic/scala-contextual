package org.kalergic.contextual.v0.context

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ContextualizedExecutionContextSpec extends AnyFlatSpec with should.Matchers {

  import ContextualizedExecutionContext.Implicits._

  class Fixture {

    val fakeEC: FakeExecutionContext = new FakeExecutionContext
    val contextualizedEC: ContextualizedExecutionContext = fakeEC.contextualized
  }

  "ContextualizedExecutionContext" should "wrap Runnables and forward the decorated Runnable to the target execution context" in new Fixture {
    val r: Runnable = () => ()
    contextualizedEC.execute(r)

    fakeEC.runnables.size shouldBe 1
    fakeEC.runnables.map(_.target) should contain(r)
  }

  it should "not re-wrap ContextualizedRunnables" in new Fixture {
    val cr = new ContextualizedRunnable(() => (), new FakeThreadLocalContext)
    contextualizedEC.execute(cr)
    fakeEC.runnables.size shouldBe 1
    assert(fakeEC.runnables.head eq cr)
  }

  it should "not re-wrap ContextualizedExecutionContext objects" in new Fixture {
    val cec: ContextualizedExecutionContext = contextualizedEC.contextualized
    assert(cec eq contextualizedEC)
  }

  it should "report failures to the underlying execution context" in new Fixture {
    val throwable = new Exception("Test exception")
    contextualizedEC.reportFailure(throwable)
    fakeEC.throwables shouldBe Seq(throwable)
  }
}
