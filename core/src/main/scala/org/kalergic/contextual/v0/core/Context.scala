package org.kalergic.contextual.v0.core

import scala.reflect.runtime.universe._

trait Context {
  def put[V: TypeTag](contextKey: ContextKey[V], value: V): Option[V]
  def get[V](contextKey: ContextKey[V]): Option[V]
  def remove[V: TypeTag](contextKey: ContextKey[V]): Option[V]
  def clear(): Unit
}
