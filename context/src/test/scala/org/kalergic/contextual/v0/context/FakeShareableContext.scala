package org.kalergic.contextual.v0.context

class FakeShareableContext extends FakeContext with ShareableContext {

  @volatile var copyCallCount = 0
  @volatile var activateCallCount = 0
  @volatile var deactivateCallCount = 0

  override private[context] def copy(): ShareableContext = {
    copyCallCount += 1
    val newFake = new FakeShareableContext
    newFake.dataMap = dataMap
    newFake
  }

  override private[context] def activateForCurrentThread(): Unit = activateCallCount += 1

  override private[context] def deactivateForCurrentThread(): Unit = deactivateCallCount += 1
}
