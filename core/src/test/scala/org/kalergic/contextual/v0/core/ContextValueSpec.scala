package org.kalergic.contextual.v0.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ContextValueSpec extends AnyFlatSpec with should.Matchers {

  import ContextMap._

  trait ContextValueFixture[V] {
    def contextValue: ContextValue[V]
    def expectedValue: V
    var valuePassed: Option[V] = None
    def someFun[T](key: ContextKey[T])(t: T): Unit = valuePassed = Some(t.asInstanceOf[V])
  }

  class ContextValueStringFixture extends ContextValueFixture[String] {
    val expectedValue = "Foo"
    val contextValue: ContextValue[String] = ContextValue[String](expectedValue)
  }

  class ContextValueStringSeqFixture extends ContextValueFixture[Seq[String]] {
    val expectedValue = Seq("Hello", "World")
    val contextValue: ContextValue[Seq[String]] = ContextValue[Seq[String]](expectedValue)
  }

  class ContextValueStringListFixture extends ContextValueFixture[List[String]] {
    val expectedValue = List("Foo", "Bar", "Baz")
    val contextValue: ContextValue[List[String]] = ContextValue[List[String]](expectedValue)
  }

  "ContextValue" should "validate types and invoke the function if the types agree exactly" in new ContextValueStringFixture {
    contextValue.checkTypeAndInvoke(ContextKey.forType[String]("keyname"), someFun[String])
    valuePassed should contain(expectedValue)
  }

  it should "validate types and invoke the function if the higher-kinded types agree exactly" in new ContextValueStringListFixture {
    contextValue.checkTypeAndInvoke(ContextKey.forType[List[String]]("keyname"), someFun[List[String]])
    valuePassed should contain(expectedValue)
  }

  it should "validate types and invoke the function if the declared value type is a subtype of the type specified in the target function's type parameter" in new ContextValueStringListFixture {
    contextValue.checkTypeAndInvoke(ContextKey.forType[List[String]]("keyname"), someFun[Seq[String]])
    valuePassed should contain(expectedValue)
  }

  it should "throw a ClassCastException if the declared value type is a supertype of the type specified in the target function's type parameter" in new ContextValueStringSeqFixture {
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(ContextKey.forType[Seq[String]]("keyname"), someFun[List[String]])
    }
    valuePassed shouldBe empty
  }

  it should "validate types and invoke the function if the declared value type is a subtype of the type declared in the key's type parameter'" in new ContextValueStringListFixture {
    contextValue.checkTypeAndInvoke(
      ContextKey.forType[Seq[String]]("keyname").asInstanceOf[ContextKey[List[String]]],
      someFun[List[String]]
    )
    valuePassed should contain(expectedValue)
  }

  it should "throw a ClassCastException if the declared value type is a supertype of the type declared in the key's type parameter" in new ContextValueStringSeqFixture {
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(ContextKey.forType[Seq[String]]("keyname"), someFun[List[String]])
    }
    valuePassed shouldBe empty
  }

  it should "throw a ClassCastException if the declared value type is completely disjoint from the type specified in the target function's type parameter" in new ContextValueStringFixture {
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(ContextKey.forType[String]("keyname"), someFun[Int])
    }
    valuePassed shouldBe empty
  }

  it should "throw a ClassCastException if the declared value type is completely disjoint from the type declared in the key's type parameter" in new ContextValueStringFixture {
    // This will compile, because the static type is just ContextKey with no type parameter (type erasure!)
    val theKey: ContextKey[String] = ContextKey.forType[Int]("keyname").asInstanceOf[ContextKey[String]]
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(theKey, someFun[Any])
    }
    valuePassed shouldBe empty
  }

  it should "throw a ClassCastException if all types are disjoint" in new ContextValueStringFixture {
    // This will compile, because the static type is just ContextKey with no type parameter (type erasure!)
    val theKey: ContextKey[String] = ContextKey.forType[Double]("keyname").asInstanceOf[ContextKey[String]]
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(theKey, someFun[Int])
    }
  }
}
