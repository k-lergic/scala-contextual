package org.kalergic.contextual.v0.context

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ContextualSpec extends AnyFlatSpec with should.Matchers {

  class ContextualFixture {
    val fakeContext: FakeContext with FakeListenerManager = new FakeListenerManager with FakeContext
    val contextual = new Contextual(backing = fakeContext)

    val testkey: ContextKey[Int] = ContextKey.forType[Int]("testkey")

    val fakeListener = new FakeListener[Int](testkey)

    assume(fakeContext.getCallCount === 0)
    assume(fakeContext.putCallCount === 0)
    assume(fakeContext.removeCallCount === 0)
    assume(fakeContext.clearCallCount === 0)
    assume(fakeContext.addListenerCallCount === 0)
    assume(fakeContext.removeListenerCallCount === 0)

  }

  "The Contextual companion object" should "return an instance of Contextual backed by the GlobalContext" in {
    val contextual = Contextual()
    contextual.backing shouldBe GlobalContext
  }

  it should "call the backing context store on a get" in new ContextualFixture {
    contextual.get(testkey)
    fakeContext.getCallCount shouldBe 1
  }

  it should "call the backing context store on a put" in new ContextualFixture {
    contextual.put(testkey, 15)
    fakeContext.putCallCount shouldBe 1
  }

  it should "return the value returned by the backing context store on a put" in new ContextualFixture {
    contextual.put(testkey, 15)
    contextual.put(testkey, 9) should contain(15)
  }

  it should "return an empty option if an empty option is returned by the backing context store on a put" in new ContextualFixture {
    contextual.put(testkey, 9) shouldBe empty
  }

  it should "call the backing context store on a remove" in new ContextualFixture {
    contextual.remove(testkey)
    fakeContext.removeCallCount shouldBe 1
  }

  it should "return an empty option if an empty option is returned by the backing context store on a remove" in new ContextualFixture {
    contextual.remove(testkey) shouldBe empty
  }

  it should "return the value returned by the backing context store on a remove" in new ContextualFixture {
    contextual.put(testkey, 15)
    contextual.remove(testkey) should contain(15)
  }

  it should "call the backing context store on a clear" in new ContextualFixture {
    contextual.clear()
    fakeContext.clearCallCount shouldBe 1
  }

  it should "call the backing context store on addListener" in new ContextualFixture {
    contextual.addListener(fakeListener)
    fakeContext.addListenerCallCount shouldBe 1
  }

  it should "call the backing context store on removeListener" in new ContextualFixture {
    contextual.removeListener(fakeListener)
    fakeContext.removeListenerCallCount shouldBe 1
  }
}
