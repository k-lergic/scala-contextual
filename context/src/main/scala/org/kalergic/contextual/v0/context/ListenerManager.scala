package org.kalergic.contextual.v0.context

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe._
import scala.util.control.NonFatal

import com.typesafe.scalalogging.StrictLogging

trait ListenerManager {
  def addListener[V](listener: ContextValueListener[V]): Unit
  def removeListener[V](listener: ContextValueListener[V]): Unit
}

private[context] trait ListenerNotifier { self: ListenerManager =>
  private[context] def notifyPut[V: TypeTag](key: ContextKey[V])(value: V): Unit
  private[context] def notifyRemove[V: TypeTag](key: ContextKey[V])(value: V): Unit
}

private[context] class ListenerManagerImpl extends ListenerManager with ListenerNotifier with StrictLogging {

  private[this] val listeners: mutable.Set[ContextValueListener[_]] =
    java.util.concurrent.ConcurrentHashMap.newKeySet[ContextValueListener[_]].asScala

  def currentListeners: Seq[ContextValueListener[_]] = Seq(listeners.toSeq: _*)

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
    currentListeners.filter(l => l.contextKey == key && typeOf[V] <:< l.valueTypeTag.tpe).foreach {
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
