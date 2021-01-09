package org.kalergic.contextual.v0.context

import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class GlobalContextSpec extends AnyFlatSpec with should.Matchers with BeforeAndAfterEach {

  val k1: ContextKey[Int] = ContextKey.forType[Int]("k1")
  val k2: ContextKey[Int] = ContextKey.forType[Int]("k2")

  // Clear thread local data before each test runs
  override def beforeEach(): Unit = GlobalContext.threadLocalContext.set(None)

  "GlobalContext" should "put the first key/value pair and save the backing ThreadContext" in {
    assume(GlobalContext.currentThreadContext.isEmpty)
    GlobalContext.put(k1, 15)
    GlobalContext.currentThreadContext.flatMap(_.get(k1)) should contain(15)
  }

  it should "return the current backing ThreadContext" in {
    assume(GlobalContext.currentThreadContext.isEmpty)
    assume(GlobalContext.threadLocalContext.get.isEmpty)
    GlobalContext.put(k1, 15)
    assert(GlobalContext.currentThreadContext eq GlobalContext.threadLocalContext.get)
  }

  it should "use an existing backing ThreadContext when there is already context data" in {
    assume(GlobalContext.currentThreadContext.isEmpty)

    GlobalContext.put(k1, 15)
    assume(GlobalContext.currentThreadContext.flatMap(_.get(k1)) contains 15)

    val backingContextAfterFirstPut: Option[ShareableContext] = GlobalContext.threadLocalContext.get
    assume(backingContextAfterFirstPut.nonEmpty)

    GlobalContext.put(k2, 16)

    val backingContextAfterSecondPut: Option[ShareableContext] = GlobalContext.threadLocalContext.get
    assume(backingContextAfterFirstPut.nonEmpty)

    // Make sure the internal backing context is the correct one
    assert(backingContextAfterFirstPut.get eq backingContextAfterSecondPut.get)

    assert(GlobalContext.currentThreadContext.flatMap(_.get(k2)) contains 16)

    // We should still have the previously set value
    assert(GlobalContext.currentThreadContext.flatMap(_.get(k1)) contains 15)
  }

  it should "return the previously set value on a put, if any" in {
    GlobalContext.put(k1, 15) shouldBe empty
    GlobalContext.put(k1, 16) should contain(15)
  }

  it should "get values for keys that have been stored" in {
    GlobalContext.put(k1, 15)
    GlobalContext.put(k2, 16)
    GlobalContext.get(k1) should contain(15)
    GlobalContext.get(k2) should contain(16)
  }

  it should "get an empty option for keys that are not stored" in {
    GlobalContext.get(k1) shouldBe empty
    GlobalContext.get(k2) shouldBe empty

    GlobalContext.put(k1, 15)

    assume(GlobalContext.get(k1).contains(15))
    GlobalContext.get(k2) shouldBe empty
  }

  it should "return the previously set value on a remove, if any" in {
    GlobalContext.put(k1, 15)

    assume(GlobalContext.get(k1).contains(15))
    assume(GlobalContext.get(k2).isEmpty)
    GlobalContext.remove(k1) should contain(15)
    GlobalContext.remove(k2) shouldBe empty
  }

  it should "remove values and retain data for keys that are not removed" in {
    GlobalContext.put(k1, 15)
    GlobalContext.put(k2, 16)

    assume(GlobalContext.get(k1).contains(15))
    GlobalContext.remove(k1)
    GlobalContext.get(k1) shouldBe empty
  }

  it should "clear ThreadLocal storage if no data remain after a remove" in {
    GlobalContext.put(k1, 15)
    GlobalContext.put(k2, 16)

    GlobalContext.remove(k1)
    assume(GlobalContext.threadLocalContext.get.nonEmpty)
    assume(GlobalContext.get(k1).isEmpty)
    assume(GlobalContext.get(k2).contains(16))

    GlobalContext.remove(k2)
    assume(GlobalContext.get(k2).isEmpty)
    GlobalContext.threadLocalContext.get shouldBe empty
  }

  it should "clear ThreadLocal storage when clear is called" in {
    GlobalContext.put(k1, 15)
    GlobalContext.put(k2, 16)

    val contextToBeCleared: Option[ShareableContext] = GlobalContext.threadLocalContext.get
    assume(contextToBeCleared.nonEmpty)
    assume(contextToBeCleared.get.nonEmpty)
    GlobalContext.clear()
    GlobalContext.threadLocalContext.get shouldBe empty
  }

  it should "clear even if there is no data" in {
    assume(GlobalContext.threadLocalContext.get.isEmpty)
    GlobalContext.clear()
    GlobalContext.threadLocalContext.get shouldBe empty
  }
}
