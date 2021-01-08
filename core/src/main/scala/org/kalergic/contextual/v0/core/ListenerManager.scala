package org.kalergic.contextual.v0.core

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe._
import scala.util.control.NonFatal

import com.typesafe.scalalogging.StrictLogging

trait ListenerManager {
  def addListener[V](listener: ContextValueListener[V]): Unit
  def removeListener[V](listener: ContextValueListener[V]): Unit
}

private[core] trait ListenerNotifier { self: ListenerManager =>
  def notifyPut[V: TypeTag](key: ContextKey[V])(value: V): Unit
  def notifyRemove[V: TypeTag](key: ContextKey[V])(value: V): Unit
}

private[core] class ListenerManagerImpl extends ListenerManager with ListenerNotifier with StrictLogging {

  private[this] val listeners: mutable.Set[ContextValueListener[_]] =
    java.util.concurrent.ConcurrentHashMap.newKeySet[ContextValueListener[_]].asScala

  override def addListener[V](listener: ContextValueListener[V]): Unit = listeners.add(listener)
  override def removeListener[V](listener: ContextValueListener[V]): Unit = listeners.remove(listener)

  override def notifyPut[V: TypeTag](key: ContextKey[V])(value: V): Unit = notifyChange(key)(value)(_.put)

  override def notifyRemove[V: TypeTag](key: ContextKey[V])(value: V): Unit = notifyChange(key)(value)(_.removed)

  private[this] def notifyChange[V: TypeTag](
    key: ContextKey[V]
  )(
    value: V
  )(
    updateFn: ContextValueListener[V] => V => Unit
  ): Unit =
    Seq(listeners.toSeq: _*).foreach {
      case listener: ContextValueListener[V @unchecked] =>
        try {
          updateFn(listener)(value)
        } catch {
          case NonFatal(e) =>
            logger.error(s"Unable to update listener=${listener.name} for key=$key", e)
        }
      case wronglyTyped =>
        logger.error(
          s"Type error: Listener has wrong type, required=${typeOf[V]}, found=${wronglyTyped.valueTypeTag.tpe}"
        )
    }
}
