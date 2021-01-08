package org.kalergic.contextual.v0.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ContextImplSpec extends AnyFlatSpec with should.Matchers {

  class ThreadContextFixture {
    val testKey: ContextKey[String] = ContextKey.forType[String]("testkey")
    val fakeManagerNotifier: FakeListenerManager = new FakeListenerManager {}
    val context = new ContextImpl(notifier = fakeManagerNotifier)
  }

  "ContextImpl" should "store and retrieve" in new ThreadContextFixture {
    context.put(testKey, "four")
    context.get(testKey) should contain("four")
  }

  it should "store and retrieve multiple values" in new ThreadContextFixture {
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")
    context.put(k1, "four")
    context.put(k2, 7)

    context.get(k1) should contain("four")
    context.get(k2) should contain(7)
  }

  it should "replace a value on a put" in new ThreadContextFixture {
    context.put(testKey, "four")
    context.put(testKey, "five")
    context.get(testKey) should contain("five")
  }

  it should "return the replaced value on a put" in new ThreadContextFixture {
    context.put(testKey, "four")
    context.put(testKey, "eight") should contain("four")
  }

  it should "return an empty option on a put when the key was not present" in new ThreadContextFixture {
    context.put(testKey, "eight") shouldBe empty
  }

  it should "return an empty option on a get when the key was not present" in new ThreadContextFixture {
    context.get(testKey) shouldBe empty
  }

  it should "remove a value" in new ThreadContextFixture {
    context.put(testKey, "four")
    context.remove(testKey)
    context.get(testKey) shouldBe empty
  }

  it should "return the removed value on a remove" in new ThreadContextFixture {
    context.put(testKey, "four")
    context.remove(testKey) should contain("four")
  }

  it should "return an empty option on a remove when the key was not present" in new ThreadContextFixture {
    context.remove(testKey) shouldBe empty
  }

  it should "clear all context data" in new ThreadContextFixture {
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")

    context.put(k1, "hi")
    context.put(k2, 77)
    assume(context.size === 2)
    assume(context.nonEmpty)

    context.clear()
    context.size shouldBe 0
    context.dataMap shouldBe empty
    context shouldBe empty
    context.keys.toSeq shouldBe empty

  }

  it should "support higher-kinded types" in new ThreadContextFixture {
    val k: ContextKey[Seq[String]] = ContextKey.forType[Seq[String]]("seqstringkey")
    context.put(k, Seq("x", "y"))
    context.get(k) should contain(Seq("x", "y"))
    context.remove(k) should contain(Seq("x", "y"))
    context.get(k) shouldBe empty
    context.put(k, Seq("a", "b"))
    context.get(k) should contain(Seq("a", "b"))
    context.clear()
    context.get(k) shouldBe empty
  }

  it should "report the correct size" in new ThreadContextFixture {
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")
    context.put(k1, "six")
    context.size shouldBe 1
    context.put(k2, 77)
    context.size shouldBe 2
    context.remove(k1)
    context.size shouldBe 1
    context.clear()
    context.size shouldBe 0
  }

  it should "provide all the keys" in new ThreadContextFixture {
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")

    context.put(k1, "foo")
    context.put(k2, 33)

    context.keys.toSeq should contain theSameElementsAs Seq(k1, k2)

    context.remove(k1)
    context.keys.toSeq shouldBe Seq(k2)

    context.clear()
    context.keys shouldBe empty
  }

  it should "report emptiness and non-emptiness status" in new ThreadContextFixture {
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")

    context.isEmpty shouldBe true
    context.nonEmpty shouldBe false

    context.put(k1, "foo")
    context.isEmpty shouldBe false
    context.nonEmpty shouldBe true

    context.put(k2, 99)
    context.isEmpty shouldBe false
    context.nonEmpty shouldBe true

    context.remove(k1)
    context.isEmpty shouldBe false
    context.nonEmpty shouldBe true

    context.clear()
    context.isEmpty shouldBe true
    context.nonEmpty shouldBe false
  }

  it should "notify listeners on a put" in new ThreadContextFixture {
    import FakeListenerManager._
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")

    context.put(k1, "foo")
    context.put(k2, 9)
    fakeManagerNotifier.puts.keys shouldBe Seq(k1, k2)
    fakeManagerNotifier.puts.values shouldBe Seq("foo", 9)
  }

  it should "notify listeners on a remove" in new ThreadContextFixture {
    import FakeListenerManager._
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")

    context.put(k1, "foo")
    context.put(k2, 9)
    context.remove(k1)
    context.remove(k2)

    fakeManagerNotifier.removes.keys shouldBe Seq(k1, k2)
    fakeManagerNotifier.removes.values shouldBe Seq("foo", 9)
  }

  it should "notify listeners on a clear" in new ThreadContextFixture {
    import FakeListenerManager._
    val k1: ContextKey[String] = testKey
    val k2: ContextKey[Int] = ContextKey.forType[Int]("intkey")

    context.put(k1, "foo")
    context.put(k2, 9)
    context.clear()
    context.dataMap shouldBe empty

    fakeManagerNotifier.removes.keys shouldBe Seq(k1, k2)
    fakeManagerNotifier.removes.values shouldBe Seq("foo", 9)
  }

  it should "construct a new instance of ThreadContext when copy is called" in new ThreadContextFixture {
    val newContext: ContextImpl = context.copy()
    assert(newContext ne context)
  }

  it should "call copy on its data map and pass the result to the copy it is constructing when copy is called" in new ThreadContextFixture {
    val newContext: ContextImpl = context.copy()
    assume(newContext ne context)
    assert(newContext.dataMap ne context.dataMap)
  }

  it should "pass its listener notifier instance to copies it constructs" in new ThreadContextFixture {
    val newContext: ContextImpl = context.copy()
    assume(newContext ne context)
    assert(newContext.notifier eq context.notifier)
  }

  it should "notify listeners when it is activated for a thread" in new ThreadContextFixture {
    import FakeListenerManager._

    val ck1: ContextKey[String] = testKey
    val ck2: ContextKey[Int] = ContextKey.forType[Int]("bar")

    context.put(ck1, "foo")
    context.put(ck2, 99)

    // Clear the notification data, as it will be re-populated when the activate method is called
    fakeManagerNotifier.puts = Seq.empty

    context.activateForCurrentThread()

    fakeManagerNotifier.puts.keys shouldBe Seq(ck1, ck2)
    fakeManagerNotifier.puts.values shouldBe Seq("foo", 99)
  }

  it should "clear all context and notify listeners when it is deactivated for a thread" in new ThreadContextFixture {
    import FakeListenerManager._
    val ck1: ContextKey[String] = testKey
    val ck2: ContextKey[Int] = ContextKey.forType[Int]("bar")

    context.put(ck1, "foo")
    context.put(ck2, 99)

    context.deactivateForCurrentThread()
    context shouldBe empty
    fakeManagerNotifier.removes.keys shouldBe Seq(ck1, ck2)
    fakeManagerNotifier.removes.values shouldBe Seq("foo", 99)
  }

}
