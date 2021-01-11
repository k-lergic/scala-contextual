# scala-contextual
General purpose, lightweight, decorator-based Scala library that enables passing context information across threads as
asynchronous computations execute, _without_ requiring method signature changes (e.g. Context Functions in Dotty).

## Usage

### Core library: contextual
```sbt
libraryDependencies += "org.kalergic.contextual" % "scala-contextual-context" % "x.y.z"
```
```
import org.kalergic.contextual.v0.contextualize._

case class SomeId(id: String) extends Contextualizable

implicit val ec: ExecutionContext = contextualized(someScalaExecutionContext)

contextualize(SomeId("foo"))

// You can summon the data you contextualized in this thread
val someId = summon[SomeId]

Future {
  // You can summon the data you contextualized above in some other thread
  val someId = summon[SomeId] // The value summoned is Some(SomeId("foo"))

  // do something

  Future {
    // You can keep going, any future to which you delegate will be able to summon the value
    val summonAgain = summon[SomeId] // The value is the same: Some(SomeId("foo"))

    // You can also overwrite it
    contextualize(SomeId("bar"))

    // And this value will be available here and in futures to which you delegate
    Future {
      val andAgain = summon[SomeId] // The value is Some(SomeId("bar"))
    }
  }

  // But out here, you will still have the original contextualized value (no race condition)
  val summonAgain = summmon[SomeId] // The value is Some(SomeId("foo"))
}

// And out here too (no race condition)

val anotherSummon = sommon[SomeId] // The value is Some(SomeId("foo"))
```
See [Examples](#included-code-examples)
See [Unit Tests](context/src/test/scala/org/kalergic/contextual/v0/contextualize/ContextualizeSpec.scala)

### Correlation Id MDC Support in slf4j with logback
```sbt
libraryDependencies += "org.kalergic.contextual" % "scala-contextual-correlation" % "x.y.z"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "a.b.c"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "d.e.f"
```

```
import java.util.UUID
import org.kalergic.contextual.v0.contextualize._
import org.kalergic.contextual.v0.correlation._

implicit val ec: ExecutionContext = contextualized(someScalaExecutionContext)
CorrelationIdSupport.install()


contextualize(CorrelationId(UUID.randomUUID.toString))

logger.info("Some message") // Includes correlation id in output

Future {
  logger.info("Some message") // Includes correlation id in output
  // do something
  Future {
    logger.info("Some message") // Includes correlation id in output

    // You can also overwrite it
    contextualize(CorrelationId("my-custom-correlation-id"))

    // And this value will be available here and in futures to which you delegate

    Future {
      logger.info("Some message") // Includes your overwrriten correlation id in output
    }
  }

  // But out here, you will still have the original contextualized value -- no race condition
  logger.info("Some message") // Includes original correlation id
}
// And also out here -- no race condition
logger.info("Some message") // Includes original correlation id

```
See [Examples](#included-code-examples)
See [Unit Tests](correlation/src/test/scala/org/kalergic/contextual/v0/correlation/CorrelationIdSupportSpec.scala)

## Dependencies

### Prerequisite for building: Build the sbt-shading plugin locally.

The [**sbt-shading**](https://github.com/rallyhealth/sbt-shading) plugin enforces that components of a library are versioned. This
prevents dependency hell in complex codebases with multiple dependencies, some of them pulled in transitively, by allowing major
versions of a library to coexist in the same codebase.

As of this writing, the **sbt-shading** plugin must be cloned and published to your local Ivy cache; our friends at
[Rally](https://www.rallyhealth.com) have not yet published binaries for it.

```sbt
git clone git@github.com:rallyhealth/sbt-shading.git
cd sbt-shading
sbt publishLocal
```

### Versioning
Versioning for this library is handled by [**sbt-git-versioning**](https://github.com/rallyhealth/sbt-git-versioning), which
**does** have binaries published for it.

## Included Code Examples

- [Basic Futures Example](examples/src/main/scala/org/kalergic/contextual/examples/futures/FuturesExample.scala)
  - Contextualizing and summoning context data
  - Integration with slf4j / logback MDC
- [Akka Streams Example](examples/src/main/scala/org/kalergic/contextual/examples/akkastreams/PrintTweetHashtags.scala)
  - Demonstrates slf4j / logback MDC integration in an Akka streams application

## Future work

- Akka Actors example
- [Failsafe example](https://jodah.net/failsafe/)
  - Ensure context is passed to different threads as retries are attempted
- Play Framework examples:
  - Wiring up a correlation id for HTTP request processing within asynchronous computations
  - Play body parsers example
