package org.kalergic.contextual.v0.core

private[core] trait ShareableContext extends Context {
  private[core] def copy(): ShareableContext
  private[core] def activateForCurrentThread(): Unit
  private[core] def deactivateForCurrentThread(): Unit
}
