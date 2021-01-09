package org.kalergic.contextual.v0.context

private[context] class FakeThreadLocalContext extends FakeListenerManager with FakeContext with ThreadLocalContext {

  @volatile var setParamValues: Seq[Option[ShareableContext]] = Seq.empty
  @volatile var snapshotCount: Int = 0
  @volatile var setCount: Int = 0

  override def currentThreadContext: Option[ShareableContext] = Some(new FakeShareableContext)

  override def snapshot: Option[ShareableContext] = {
    snapshotCount += 1
    Some(new FakeShareableContext)
  }

  override def setContext(context: Option[ShareableContext]): Unit = {
    setCount += 1
    setParamValues :+= context
  }
}
