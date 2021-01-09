package org.kalergic.contextual.v0.context

import scala.reflect.runtime.universe._

trait Context {
  def put[V: TypeTag](key: ContextKey[V], value: V): Option[V]
  def get[V](key: ContextKey[V]): Option[V]
  def remove[V: TypeTag](key: ContextKey[V]): Option[V]
  def clear(): Unit
}
