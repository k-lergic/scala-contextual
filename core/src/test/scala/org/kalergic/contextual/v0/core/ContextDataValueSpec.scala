package org.kalergic.contextual.v0.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ContextDataValueSpec extends AnyFlatSpec with should.Matchers {

  import ContextDataMap._

  trait ContextValueFixture[V] {
    def contextValue: ContextDataValue[V]
    def expectedValue: V
    var valuePassed: Option[V] = None
    def someFun[T](key: ContextKey[T])(t: T): Unit = valuePassed = Some(t.asInstanceOf[V])
  }

  class ContextValueStringFixture extends ContextValueFixture[String] {
    val expectedValue = "Foo"
    val contextValue: ContextDataValue[String] = ContextDataValue[String](expectedValue)
  }

  class ContextValueStringSeqFixture extends ContextValueFixture[Seq[String]] {
    val expectedValue = Seq("Hello", "World")
    val contextValue: ContextDataValue[Seq[String]] = ContextDataValue[Seq[String]](expectedValue)
  }

  class ContextValueStringListFixture extends ContextValueFixture[List[String]] {
    val expectedValue = List("Foo", "Bar", "Baz")
    val contextValue: ContextDataValue[List[String]] = ContextDataValue[List[String]](expectedValue)
  }

  "ContextValue" should "validate types and invoke the function if the types agree exactly" in new ContextValueStringFixture {
    contextValue.checkTypeAndInvoke(ContextKey.forType[String]("testkey"), someFun[String])
    valuePassed should contain(expectedValue)
  }

  it should "validate types and invoke the function if the higher-kinded types agree exactly" in new ContextValueStringListFixture {
    contextValue.checkTypeAndInvoke(ContextKey.forType[List[String]]("testkey"), someFun[List[String]])
    valuePassed should contain(expectedValue)
  }

  it should "validate types and invoke the function if the declared value type is a subtype of the type specified in the target function's type parameter" in new ContextValueStringListFixture {
    contextValue.checkTypeAndInvoke(ContextKey.forType[List[String]]("testkey"), someFun[Seq[String]])
    valuePassed should contain(expectedValue)
  }

  it should "throw a ClassCastException if the declared value type is a supertype of the type specified in the target function's type parameter" in new ContextValueStringSeqFixture {
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(ContextKey.forType[Seq[String]]("testkey"), someFun[List[String]])
    }
    valuePassed shouldBe empty
  }

  it should "validate types and invoke the function if the declared value type is a subtype of the type declared in the key's type parameter'" in new ContextValueStringListFixture {
    contextValue.checkTypeAndInvoke(
      ContextKey.forType[Seq[String]]("testkey").asInstanceOf[ContextKey[List[String]]],
      someFun[List[String]]
    )
    valuePassed should contain(expectedValue)
  }

  it should "throw a ClassCastException if the declared value type is a supertype of the type declared in the key's type parameter" in new ContextValueStringSeqFixture {
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(ContextKey.forType[Seq[String]]("testkey"), someFun[List[String]])
    }
    valuePassed shouldBe empty
  }

  it should "throw a ClassCastException if the declared value type is completely disjoint from the type specified in the target function's type parameter" in new ContextValueStringFixture {
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(ContextKey.forType[String]("testkey"), someFun[Int])
    }
    valuePassed shouldBe empty
  }

  it should "throw a ClassCastException if the declared value type is completely disjoint from the type declared in the key's type parameter" in new ContextValueStringFixture {
    // This will compile, because the static type is just ContextKey with no type parameter (type erasure!)
    val theKey: ContextKey[String] = ContextKey.forType[Int]("testkey").asInstanceOf[ContextKey[String]]
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(theKey, someFun[Any])
    }
    valuePassed shouldBe empty
  }

  it should "throw a ClassCastException if all types are disjoint" in new ContextValueStringFixture {
    // This will compile, because the static type is just ContextKey with no type parameter (type erasure!)
    val theKey: ContextKey[String] = ContextKey.forType[Double]("testkey").asInstanceOf[ContextKey[String]]
    intercept[ClassCastException] {
      contextValue.checkTypeAndInvoke(theKey, someFun[Int])
    }
  }
}
