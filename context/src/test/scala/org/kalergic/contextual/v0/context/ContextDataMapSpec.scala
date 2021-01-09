package org.kalergic.contextual.v0.context

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ContextDataMapSpec extends AnyFlatSpec with should.Matchers {
  import ContextDataMap._

  val stringKey: ContextKey[String] = ContextKey.forType[String]("testkey")

  "ContextDataMap" should "put and get" in {
    val cm = new ContextDataMap
    cm.put(stringKey, ContextDataValue("hello"))
    cm.get(stringKey) should contain(ContextDataValue("hello"))
  }

  it should "put and apply" in {
    val cm = new ContextDataMap
    cm.put(stringKey, ContextDataValue("world"))
    cm(stringKey) shouldBe ContextDataValue("world")
  }

  it should "put and get multiple values" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Int] = ContextKey.forType[Int]("somekeyname")
    val cm = new ContextDataMap

    cm.put(key1, ContextDataValue("howdy"))
    cm.put(key2, ContextDataValue(77))

    cm.get(key1) should contain(ContextDataValue("howdy"))
    cm.get(key2) should contain(ContextDataValue(77))
  }

  it should "return None on a get call when the key is not present" in {
    val cm = new ContextDataMap
    cm.get(stringKey) shouldBe empty
  }

  it should "throw a NoSuchElementException on apply if the key is not present" in {
    val cm = new ContextDataMap

    intercept[NoSuchElementException] {
      cm(stringKey)
    }
  }

  it should "remove data for a key" in {
    val cm = new ContextDataMap
    cm.put(stringKey, ContextDataValue("aa"))
    assume(cm(stringKey) === ContextDataValue("aa"))
    cm.remove(stringKey) should contain(ContextDataValue("aa"))
    cm.get(stringKey) shouldBe empty
  }

  it should "return None on a put call if the key is not present" in {
    val cm = new ContextDataMap
    cm.put(stringKey, ContextDataValue("eight")) shouldBe empty
  }

  it should "return None on a remove call if the key is not present" in {
    val cm = new ContextDataMap
    cm.remove(stringKey) shouldBe empty
  }

  it should "overwrite a value when the key is already present" in {
    val cm = new ContextDataMap
    cm.put(stringKey, ContextDataValue("oz"))
    assume(cm(stringKey) === ContextDataValue("oz"))
    cm.put(stringKey, ContextDataValue("kansas"))
    cm(stringKey) shouldBe ContextDataValue("kansas")
  }

  it should "support higher-kinded types" in {
    val cm = new ContextDataMap
    val ck = ContextKey.forType[List[Int]]("dog")

    cm.put(ck, ContextDataValue(List(1, 2, 3)))
    val result: Option[ContextDataValue[List[Int]]] = cm.get(ck)
    result should not be empty
    result should contain(ContextDataValue(List(1, 2, 3)))
  }

  it should "report the correct size" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Int] = ContextKey.forType[Int]("cat")

    val cm = new ContextDataMap
    cm.size shouldBe 0
    cm.put(key1, ContextDataValue("toto"))
    cm.size shouldBe 1
    cm.put(key2, ContextDataValue(9))
    cm.size shouldBe 2

    cm.remove(key1)
    cm.size shouldBe 1
    cm.clear()
    cm.size shouldBe 0
  }

  it should "clear all stored data" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Int] = ContextKey.forType[Int]("dog")

    val cm = new ContextDataMap
    cm.put(key1, ContextDataValue("toto"))
    cm.put(key2, ContextDataValue(72))
    assume(cm.size === 2)

    cm.clear()
    cm.size shouldBe 0
    cm shouldBe empty
    cm.keys shouldBe empty
  }

  it should "return all the keys" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Int] = ContextKey.forType[Int]("number")

    val cm = new ContextDataMap
    cm.put(key1, ContextDataValue("seven"))

    cm.keys shouldBe Set(key1)

    cm.put(key2, ContextDataValue(7))

    cm.keys should contain theSameElementsAs Seq(key1, key2)

    cm.clear()
    cm.keys shouldBe empty

  }

  it should "return the proper result for isEmpty and nonEmpty" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Int] = ContextKey.forType[Int]("zing")

    val cm = new ContextDataMap
    cm.nonEmpty shouldBe false
    cm.isEmpty shouldBe true

    cm.put(key1, ContextDataValue("zang"))
    cm.nonEmpty shouldBe true
    cm.isEmpty shouldBe false

    cm.put(key2, ContextDataValue(89))
    cm.nonEmpty shouldBe true
    cm.isEmpty shouldBe false

    cm.remove(key2)
    cm.nonEmpty shouldBe true
    cm.isEmpty shouldBe false

    cm.clear()
    cm.nonEmpty shouldBe false
    cm.isEmpty shouldBe true
  }

  it should "return the expected value for an internal typed get" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Long] = ContextKey.forType[Long]("time")

    val cm = new ContextDataMap
    cm.put(key1, ContextDataValue("abc"))
    cm.put(key2, ContextDataValue(7L))

    cm.typedGet[String](key1) should contain(ContextDataValue("abc"))
    cm.typedGet[Long](key2) should contain(ContextDataValue(7L))
  }

  it should "return None for an internal typed get when the key is not present" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Double] = ContextKey.forType[Double]("boo")

    val cm = new ContextDataMap
    cm.put(key1, ContextDataValue("fox"))

    // Do not put a value for key2

    cm.typedGet[Double](key2) shouldBe empty
  }

  it should "throw a ClassCastException when an internal typed get is called with the wrong type parameter" in {
    val key1: ContextKey[String] = stringKey
    val cm = new ContextDataMap

    // This is somewhat contrived.
    cm.put[Int](key1.asInstanceOf[ContextKey[Int]], ContextDataValue(77))

    intercept[ClassCastException] {
      cm.typedGet(key1)
    }
  }

  it should "return the correct data value for an untyped lookup" in {
    val key1: ContextKey[String] = stringKey

    val cm = new ContextDataMap
    cm.put(key1, ContextDataValue("hound"))
    cm.untypedLookup(key1) shouldBe ContextDataValue("hound")
  }

  it should "throw a NoSuchElementException for an untyped lookup when the key is not present" in {
    val key1: ContextKey[String] = stringKey
    val key2: ContextKey[Double] = ContextKey.forType[Double]("boo")
    val cm = new ContextDataMap
    cm.put(key1, ContextDataValue("yo"))

    intercept[NoSuchElementException] {
      cm.untypedLookup(key2)
    }
  }

  it should "copy itself by constructing a new instance and sharing the same data map" in {
    val map = new ContextDataMap
    map.put(stringKey, ContextDataValue("zoo"))

    val newMap = map.copy()
    assert(newMap ne map)
    assert(newMap.data eq map.data)
  }

  it should "produce a copy of itself in which changes to the data map do not affect the data map in the source instance" in {
    val map = new ContextDataMap
    map.put(stringKey, ContextDataValue("zing"))

    val newMap = map.copy()

    // Change the data in the new map
    newMap.put(stringKey, ContextDataValue("zang"))
    assert(newMap.data ne map.data)

    // Make sure the old data does not see the update made in the copy
    map(stringKey) shouldBe ContextDataValue("zing")
    newMap(stringKey) shouldBe ContextDataValue("zang")
  }

}
