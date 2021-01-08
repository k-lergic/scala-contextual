package org.kalergic.contextual.v0.core

import scala.reflect.runtime.universe._

final case class ContextKey[V: TypeTag] private[core] (name: String, valueTypeTag: TypeTag[V])

object ContextKey {
  def forType[V: TypeTag](name: String): ContextKey[V] = ContextKey(name, typeTag[V])
}

private[core] object ContextMap {

  private[core] final case class ContextValue[V: TypeTag](value: V) {

    val valueTypeTag: TypeTag[V] = typeTag[V]

    def checkTypeAndInvoke[W: TypeTag](key: ContextKey[V], targetFn: ContextKey[W] => W => Unit): Unit = {

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
