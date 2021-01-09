package org.kalergic.contextual.v0.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ThreadLocalContextSpec extends AnyFlatSpec with should.Matchers {

  class ThreadLocalContextFixture {
    val threadLocalContext: ThreadLocalContext = new FakeListenerManager with FakeContext with ThreadLocalContext
    val fakeShareableContext: FakeShareableContext = new FakeShareableContext
    threadLocalContext.threadLocalContext.set(Some(fakeShareableContext))
  }

  "ThreadLocalContext" should "return the same instance of the ShareableContext for a thread" in new ThreadLocalContextFixture {
    val mainThreadContext: Option[ShareableContext] = threadLocalContext.currentThreadContext
    val anotherReferenceToMainThreadContext: Option[ShareableContext] = threadLocalContext.currentThreadContext
    assert(anotherReferenceToMainThreadContext eq mainThreadContext)
  }

  it should "return a different context instance for each thread" in new ThreadLocalContextFixture {
    val mainThreadContext: Option[ShareableContext] = threadLocalContext.currentThreadContext

    @volatile var otherThreadContext: Option[ShareableContext] = None

    val thread: Thread = new Thread(() => {
      threadLocalContext.set(Some(new FakeShareableContext))
      otherThreadContext = threadLocalContext.currentThreadContext
    })

    thread.start()
    thread.join()

    val postChildThreadExecuteMainThreadContext: Option[ShareableContext] = threadLocalContext.currentThreadContext

    assume(mainThreadContext.nonEmpty)
    assume(otherThreadContext.nonEmpty)
    assume(postChildThreadExecuteMainThreadContext.nonEmpty)
    assert(mainThreadContext.get ne otherThreadContext.get)
    assert(mainThreadContext.get eq postChildThreadExecuteMainThreadContext.get)
  }

  it should "create a properly constructed snapshot by calling copy on the ShareableContext" in new ThreadLocalContextFixture {
    threadLocalContext.snapshot
    fakeShareableContext.copyCallCount shouldBe 1
  }

  it should "deactivate the current thread's context, set the new context, and activate the new context for a thread when the context is set" in new ThreadLocalContextFixture {

    @volatile var oldFakeDeactivated = false
    @volatile var newFakeActivated = false

    val oldFake: FakeShareableContext = new FakeShareableContext {
      override def deactivateForCurrentThread(): Unit = {
        assume(oldFake.deactivateCallCount == 0)
        assume(newFake.activateCallCount == 0)
        super.deactivateForCurrentThread()
        oldFakeDeactivated = true
      }
    }

    // This is "set-up" so we call the ThreadLocal directly
    threadLocalContext.threadLocalContext.set(Some(oldFake))

    val newFake: FakeShareableContext = new FakeShareableContext {
      override def activateForCurrentThread(): Unit = {
        assume(oldFake.deactivateCallCount == 1)
        assume(newFake.deactivateCallCount == 0)
        super.activateForCurrentThread()
        newFakeActivated = true
      }
    }

    // This is the method under test
    threadLocalContext.set(Some(newFake))
    assume(oldFakeDeactivated)
    assume(newFakeActivated)
    assume(oldFake.activateCallCount === 0)
    assume(newFake.deactivateCallCount === 0)
    oldFake.deactivateCallCount shouldBe 1
    newFake.activateCallCount shouldBe 1
    threadLocalContext.currentThreadContext should contain(newFake)
  }

  it should "deactivate the current thread's context and set an empty context (None) when the context is set" in new ThreadLocalContextFixture {

    @volatile var oldFakeDeactivated = false

    val oldFake: FakeShareableContext = new FakeShareableContext {
      override def deactivateForCurrentThread(): Unit = {
        assume(oldFake.deactivateCallCount == 0)
        super.deactivateForCurrentThread()
        oldFakeDeactivated = true
      }
    }

    // This is "set-up" so we call the ThreadLocal directly
    threadLocalContext.threadLocalContext.set(Some(oldFake))

    // This is the method under test
    threadLocalContext.set(None)
    assume(oldFakeDeactivated)
    assume(oldFake.activateCallCount === 0)
    oldFake.deactivateCallCount shouldBe 1
    threadLocalContext.currentThreadContext shouldBe empty
  }

  it should "activate the new context for the thread and set it when the previous context for the thread is empty (None)" in new ThreadLocalContextFixture {

    @volatile var newFakeActivated = false

    // This is "set-up" so we call the ThreadLocal directly
    threadLocalContext.threadLocalContext.set(None)

    val newFake: FakeShareableContext = new FakeShareableContext {
      override def activateForCurrentThread(): Unit = {
        assume(newFake.deactivateCallCount == 0)
        super.activateForCurrentThread()
        newFakeActivated = true
      }
    }

    // This is the method under test
    threadLocalContext.set(Some(newFake))
    assume(newFakeActivated)
    assume(newFake.deactivateCallCount === 0)
    newFake.activateCallCount shouldBe 1
    threadLocalContext.currentThreadContext should contain(newFake)
  }
}
