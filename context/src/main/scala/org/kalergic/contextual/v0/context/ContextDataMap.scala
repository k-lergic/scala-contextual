package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._

final case class ContextKey[V] private (name: String, valueTypeTag: TypeTag[V])

object ContextKey {
  def forType[V: TypeTag](name: String): ContextKey[V] = ContextKey(name, typeTag[V])
}

private[context] object ContextDataMap {

  private[context] final case class ContextDataValue[V: TypeTag](value: V) {

    val valueTypeTag: TypeTag[V] = typeTag[V]

    private[context] def checkTypeAndInvoke[W: TypeTag](
      key: ContextKey[_],
      targetFn: ContextKey[W] => W => Unit
    ): Unit = {

      val keyTypeParamType = key.valueTypeTag.tpe
      val valueTypeParamType = typeOf[V]
      val targetFnTypeParamType = typeOf[W]

      // Make sure the value's type conforms to what it's supposed to be (as declared in the key)
      val keyTypeConforms = valueTypeParamType <:< keyTypeParamType

      // Make sure the type parameter for the function to call conforms to what it's supposed to be
      // (as declared in the method signature of this method)
      val targetFnTypeConforms = valueTypeParamType <:< targetFnTypeParamType

      (keyTypeConforms, targetFnTypeConforms) match {
        case (true, true) =>
          // Cast away and invoke!
          targetFn.asInstanceOf[ContextKey[V] => V => Unit](key.asInstanceOf[ContextKey[V]])(value)
        case _ =>
          throw new ClassCastException(typeMismatchMessage)
      }

      def typeMismatchMessage: String = {
        val badKeyTypeMsg =
          if (!keyTypeConforms) Some(s"Wrong key type, required=${typeOf[V]}, found=$keyTypeParamType") else None
        val badTargetFnTypeMsg =
          if (!targetFnTypeConforms)
            Some(s"Wrong target function type, required=${typeOf[V]}, found=$targetFnTypeParamType")
          else None
        Seq(badKeyTypeMsg, badTargetFnTypeMsg).flatten.mkString("; ")
      }
    }
  }
}

private[context] final class ContextDataMap {
  import ContextDataMap._

  @volatile private[context] var data: Map[ContextKey[_], ContextDataValue[_]] = Map.empty

  private[context] def put[V: TypeTag](key: ContextKey[V], value: ContextDataValue[V]): Option[ContextDataValue[V]] = {
    val oldValue = typedGet(key)
    data += key -> value
    oldValue
  }

  private[context] def remove[V](key: ContextKey[V]): Option[ContextDataValue[V]] = {
    val maybeValue: Option[ContextDataValue[V]] = get(key)
    data -= key
    maybeValue
  }

  private[context] def apply[V](key: ContextKey[V]): ContextDataValue[V] =
    typedGet(key).getOrElse(throw new NoSuchElementException(s"Key=$key not found"))

  private[context] def get[V](key: ContextKey[V]): Option[ContextDataValue[V]] = typedGet[V](key)

  private[context] def clear(): Unit = data = Map.empty

  private[context] def keys: Iterable[ContextKey[_]] = data.keys

  private[context] def isEmpty: Boolean = data.isEmpty

  private[context] def nonEmpty: Boolean = data.nonEmpty

  private[context] def size: Int = data.size

  private[context] def typedGet[V](key: ContextKey[V]): Option[ContextDataValue[V]] =
    data.get(key).map {
      case typedValue: ContextDataValue[V @unchecked] if typedValue.valueTypeTag.tpe <:< key.valueTypeTag.tpe =>
        typedValue
      case wronglyTypedValue =>
        throw new ClassCastException(
          s"Wrongly typed value: expected=${key.valueTypeTag}, found=${wronglyTypedValue.valueTypeTag}"
        )
    }

  private[context] def untypedLookup(key: ContextKey[_]): ContextDataValue[_] = data(key)

  private[context] def copy(): ContextDataMap = {
    val copied = new ContextDataMap
    copied.data = data
    copied
  }
}
