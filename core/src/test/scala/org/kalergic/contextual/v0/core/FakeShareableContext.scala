package org.kalergic.contextual.v0.core

class FakeShareableContext extends FakeContext with ShareableContext {

  @volatile var copyCallCount = 0
  @volatile var activateCallCount = 0
  @volatile var deactivateCallCount = 0

  override private[core] def copy(): ShareableContext = {
    copyCallCount += 1
    val newFake = new FakeShareableContext
    newFake.dataMap = dataMap
    newFake
  }

  override private[core] def activateForCurrentThread(): Unit = activateCallCount += 1

  override private[core] def deactivateForCurrentThread(): Unit = deactivateCallCount += 1
}
