package org.kalergic.contextual.v0.context

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ListenerManagerSpec extends AnyFlatSpec with should.Matchers {
  import FakeListener._

  class ListenerManagerFixture {
    val manager: ListenerManagerImpl = new ListenerManagerImpl
    val testKey: ContextKey[String] = ContextKey.forType[String]("testkey")
    val fakeListener: FakeListener[String] = new FakeListener[String](testKey)
  }

  "ListenerManager" should "add a listener" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    manager.currentListeners should contain(fakeListener)
  }

  it should "not contain multiple references to the same listener instance" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    assume(manager.currentListeners.contains(fakeListener))
    manager.addListener(fakeListener)
    manager.currentListeners.size shouldBe 1
  }

  it should "remove a listener" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    assume(manager.currentListeners.contains(fakeListener))
    manager.removeListener(fakeListener)
    manager.currentListeners shouldBe empty
  }

  it should "not notify removed listeners of a put" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    assume(manager.currentListeners.contains(fakeListener))
    manager.notifyPut(testKey)("hello")
    manager.removeListener(fakeListener)
    assume(manager.currentListeners.isEmpty)
    manager.notifyPut(testKey)("goodbye")
    fakeListener.puts.values shouldBe Seq("hello")
  }

  it should "not notify removed listeners of a remove" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    assume(manager.currentListeners.contains(fakeListener))
    manager.notifyPut(testKey)("hello")
    manager.removeListener(fakeListener)
    assume(manager.currentListeners.isEmpty)
    manager.notifyPut(testKey)("goodbye")
    fakeListener.puts.values shouldBe Seq("hello")
  }

  it should "update listeners on notification of a put" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    manager.notifyPut(testKey)("Hello")
    assume(fakeListener.removes.isEmpty)
    fakeListener.puts.values shouldBe Seq("Hello")
    assume(fakeListener.removes.isEmpty)
    manager.notifyPut(testKey)("World")
    fakeListener.puts.values shouldBe Seq("Hello", "World")
  }

  it should "only update the correct listeners on notification of a put" in new ListenerManagerFixture {
    manager.addListener(fakeListener)

    val keyWithDifferentType: ContextKey[Int] = ContextKey.forType[Int]("testkey")
    val keyWithDifferentName: ContextKey[String] = ContextKey.forType[String]("abc")
    val keyWithDifferentTypeAndName: ContextKey[Double] = ContextKey.forType[Double]("def")

    manager.notifyPut(testKey)("foo")
    manager.notifyPut(keyWithDifferentType)(42)
    manager.notifyPut(keyWithDifferentName)("bar")
    manager.notifyPut(keyWithDifferentTypeAndName)(3.14159)

    assume(fakeListener.removes.isEmpty)
    fakeListener.puts.values shouldBe Seq("foo")
  }

  it should "update listeners on notification of a remove" in new ListenerManagerFixture {
    manager.addListener(fakeListener)

    manager.notifyRemove(testKey)("goodbye")
    assume(fakeListener.puts.isEmpty)
    fakeListener.removes.values shouldBe Seq("goodbye")

    manager.notifyRemove(testKey)("world")
    fakeListener.removes.values shouldBe Seq("goodbye", "world")
  }

  it should "only update the correct listeners on notification of a remove" in new ListenerManagerFixture {
    manager.addListener(fakeListener)

    val keyWithDifferentType: ContextKey[Int] = ContextKey.forType[Int]("testkey")
    val keyWithDifferentName: ContextKey[String] = ContextKey.forType[String]("abc")
    val keyWithDifferentTypeAndName: ContextKey[Double] = ContextKey.forType[Double]("def")

    manager.notifyRemove(testKey)("foo")
    manager.notifyRemove(keyWithDifferentType)(42)
    manager.notifyRemove(keyWithDifferentName)("bar")
    manager.notifyRemove(keyWithDifferentTypeAndName)(3.14159)

    assume(fakeListener.puts.isEmpty)
    fakeListener.removes.values shouldBe Seq("foo")
  }

  it should "notify all registered listeners for a key on notification of a put" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    val anotherListener = new FakeListener(testKey)
    manager.addListener(anotherListener)

    assume(manager.currentListeners.toSet === Set(fakeListener, anotherListener))

    manager.notifyPut(testKey)("hello, world!")

    fakeListener.puts.values shouldBe Seq("hello, world!")
    anotherListener.puts.values shouldBe Seq("hello, world!")
  }

  it should "continue to notify registered listeners for a key on puts if another listener for that key is removed" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    val anotherListener = new FakeListener(testKey)
    manager.addListener(anotherListener)

    assume(manager.currentListeners.toSet === Set(fakeListener, anotherListener))

    manager.notifyPut(testKey)("hello, world!")

    assume(fakeListener.puts.values === Seq("hello, world!"))
    assume(anotherListener.puts.values === Seq("hello, world!"))

    manager.removeListener(anotherListener)
    manager.notifyPut(testKey)("hello, again!")

    fakeListener.puts.values shouldBe Seq("hello, world!", "hello, again!")
    anotherListener.puts.values shouldBe Seq("hello, world!")
  }

  it should "notify all registered listeners for a key on notification of a remove" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    val anotherListener = new FakeListener(testKey)
    manager.addListener(anotherListener)

    assume(manager.currentListeners.toSet === Set(fakeListener, anotherListener))

    manager.notifyRemove(testKey)("goodbye, world!")

    fakeListener.removes.values shouldBe Seq("goodbye, world!")
    anotherListener.removes.values shouldBe Seq("goodbye, world!")
  }

  it should "continue to notify registered listeners for a key on remove if another listener for that key is removed" in new ListenerManagerFixture {
    manager.addListener(fakeListener)
    val anotherListener = new FakeListener(testKey)
    manager.addListener(anotherListener)

    assume(manager.currentListeners.toSet === Set(fakeListener, anotherListener))

    manager.notifyRemove(testKey)("goodbye, world!")

    assume(fakeListener.removes.values === Seq("goodbye, world!"))
    assume(anotherListener.removes.values === Seq("goodbye, world!"))

    manager.removeListener(anotherListener)
    manager.notifyRemove(testKey)("goodbye, again!")

    fakeListener.removes.values shouldBe Seq("goodbye, world!", "goodbye, again!")
    anotherListener.removes.values shouldBe Seq("goodbye, world!")
  }

  it should "support notifications for higher-kinded types and not suffer from limitations due to type erasure" in {
    val manager: ListenerManagerImpl = new ListenerManagerImpl

    val seqStringKey = ContextKey.forType[Seq[String]]("seqString")
    val seqIntKey = ContextKey.forType[Seq[Int]]("seqString")

    val seqStringListener = new FakeListener(seqStringKey)
    val seqIntListener = new FakeListener(seqIntKey)

    manager.addListener(seqStringListener)
    manager.addListener(seqIntListener)

    manager.notifyRemove(seqStringKey)(Seq("a", "b", "c"))
    manager.notifyRemove(seqIntKey)(Seq(1, 2, 3))

    manager.notifyPut(seqStringKey)(Seq("x", "y", "z"))
    manager.notifyPut(seqIntKey)(Seq(-1, -2, -3))

    seqStringListener.removes.values shouldBe Seq(Seq("a", "b", "c"))
    seqIntListener.removes.values shouldBe Seq(Seq(1, 2, 3))

    seqStringListener.puts.values shouldBe Seq(Seq("x", "y", "z"))
    seqIntListener.puts.values shouldBe Seq(Seq(-1, -2, -3))
  }

  it should "notify listeners of put events on the thread for which the update applies" in new ListenerManagerFixture
  with ScalaFutures {
    implicit val ec: ExecutionContext = Implicits.global
    manager.addListener(fakeListener)

    @volatile var expectedThreadId: Long = _

    val f: Future[Unit] = Future {
      expectedThreadId = Thread.currentThread().getId
      manager.notifyPut(testKey)("hello")
    }

    whenReady(f) { _ =>
      fakeListener.puts.values shouldBe Seq("hello")
      fakeListener.puts.threadIds shouldBe Seq(expectedThreadId)
    }
  }

  it should "notify listeners of remove events on the thread for which the update applies" in new ListenerManagerFixture
  with ScalaFutures {
    implicit val ec: ExecutionContext = Implicits.global
    manager.addListener(fakeListener)

    @volatile var expectedThreadId: Long = _

    val f: Future[Unit] = Future {
      expectedThreadId = Thread.currentThread().getId
      manager.notifyRemove(testKey)("goodbye")
    }

    whenReady(f) { _ =>
      fakeListener.removes.values shouldBe Seq("goodbye")
      fakeListener.removes.threadIds shouldBe Seq(expectedThreadId)
    }
  }
}
