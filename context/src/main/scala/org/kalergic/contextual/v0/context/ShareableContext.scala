package org.kalergic.contextual.v0.context

private[context] trait ShareableContext extends Context {
  private[context] def copy(): ShareableContext
  private[context] def activateForCurrentThread(): Unit
  private[context] def deactivateForCurrentThread(): Unit
}
