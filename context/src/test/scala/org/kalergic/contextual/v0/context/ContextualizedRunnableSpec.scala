package org.kalergic.contextual.v0.context

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ContextualizedRunnableSpec extends AnyFlatSpec with should.Matchers {

  class ContextualizedRunnableFixture {

    val k1: ContextKey[Int] = ContextKey.forType[Int]("k1")
    val k2: ContextKey[Int] = ContextKey.forType[Int]("k2")

    var runCalled: Boolean = false
    var raiseExceptionInRun: Boolean = false

    val threadLocalContext: FakeThreadLocalContext with Runnable = new FakeThreadLocalContext with Runnable {
      override def run(): Unit = {
        runCalled = true
        // This is 2 because the context is snapshotted twice: once when the Runnable is wrapped, and once when the delegating run method
        // is called.
        if (snapshotCount != 2) fail("Context was not snapshotted")
        if (setCount != 1) fail("Context was not set")
        if (raiseExceptionInRun) throw new Exception("Intentionally thrown by test")
      }
    }

    val targetRunnable: Runnable = threadLocalContext
  }

  "ContextualizedRunnable" should "snapshot the current context and not call set construction time" in new ContextualizedRunnableFixture {
    new ContextualizedRunnable(targetRunnable, threadLocalContext)
    threadLocalContext.setCount shouldBe 0
    threadLocalContext.snapshotCount shouldBe 1
  }

  it should "call the snapshot and setContext methods in the appropriate order" in new ContextualizedRunnableFixture {
    // The run method will check that snapshot was called and that set was called once
    val cr = new ContextualizedRunnable(targetRunnable, threadLocalContext)
    assume(threadLocalContext.snapshotCount === 1)

    raiseExceptionInRun = false
    cr.run()

    assume(runCalled) // ensures checks in the run method executed

    // This is 2 because the context is snapshotted twice: once when the Runnable is wrapped, and once when the delegating run method
    // is called.
    threadLocalContext.snapshotCount shouldBe 2
    threadLocalContext.setParamValues.flatten.toSet.size shouldBe 2

    // One additional set call, which should be when the run method finishes executing
    threadLocalContext.setCount shouldBe 2
  }

  it should "call the setContext method unconditionally when the delegating run method exits if the target run method throws an exception" in new ContextualizedRunnableFixture {
    // The run method will check that snapshot was called and that set was called ContextualizedRunnableFixture
    val cr = new ContextualizedRunnable(targetRunnable, threadLocalContext)
    assume(threadLocalContext.snapshotCount === 1)

    raiseExceptionInRun = true

    intercept[Exception] {
      cr.run()
      assume(runCalled) // ensures checks in the run method executed
      threadLocalContext.setCount shouldBe 2
      threadLocalContext.setCount shouldBe 2
    }
  }

}
