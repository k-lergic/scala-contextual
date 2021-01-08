package org.kalergic.contextual.v0.core

import scala.reflect.runtime.universe._

trait ContextValueListener[V] {
  def valueTypeTag: TypeTag[V]
  def contextKey: ContextKey[V]
  def name: String
  def put(value: V): Unit
  def removed(value: V): Unit
}
