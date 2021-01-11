package org.kalergic.contextual.examples.akkastreams

import java.util.UUID

import scala.concurrent.Future

import com.typesafe.scalalogging.StrictLogging
import org.kalergic.contextual.v0.contextualize._
import org.kalergic.contextual.v0.correlation.{CorrelationId, CorrelationIdLogging}

/*
  This example is borrowed from, and modified from the following Akka Streams example, from the
  Akka documentation.

  https://doc.akka.io/docs/akka/current/stream/stream-quickstart.html#browser-embedded-example

  Modifications Copyright (c) 2021, Eric J. Fredericks

  License for Akka follows.
  ---------------------------------------------------------------------------------------------

  This software is licensed under the Apache 2 license, quoted below.

  Copyright 2009-2018 Lightbend Inc. <https://www.lightbend.com>

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy of
  the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations under
  the License.

 */
object PrintTweetHashtags extends App with StrictLogging {

  import akka.NotUsed
  import akka.actor.ActorSystem
  import akka.stream.scaladsl._
  val akkaTag = Hashtag("#akka")

  // Registers a listener
  CorrelationIdLogging.install()

  implicit val system: ActorSystem = ActorSystem(
    name = "reactive-tweets",
    defaultExecutionContext = Some(contextualized(scala.concurrent.ExecutionContext.Implicits.global))
  )

  // Must defer creation of source until materialization to contextualize correlation id.
  val tweets: Source[Tweet, Future[NotUsed]] = Source.fromMaterializer[Tweet, NotUsed] { (_, _) =>
    contextualize(CorrelationId(UUID.randomUUID.toString))
    Source(
      Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
        Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
        Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
        Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
        Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
        Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
        Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
        Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
        Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
        Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
        Nil
    )
  }

  final case class Author(handle: String)

  final case class Hashtag(name: String)

  final case class Tweet(author: Author, timestamp: Long, body: String) {

    def hashtags: Set[Hashtag] =
      body
        .split(" ")
        .collect {
          case t if t.startsWith("#") => Hashtag(t.replaceAll("[^#\\w]", ""))
        }
        .toSet
  }

  tweets
    .map { tw =>
      logger.info("Getting sets of hashtags...")
      tw.hashtags
    }
    .reduce { (h1, h2) =>
      logger.info("Reducing hashtags to a single set, removing duplicates across all tweets")
      h1 ++ h2 // ... and reduce them to a single set, removing duplicates across all tweets
    }
    .mapConcat { h =>
      logger.info("Flattening the set of hastags to a stream of hashtags")
      h
    }
    .map { h =>
      logger.info("Converting hashtags to upper-case")
      h.name.toUpperCase
    }
    .runWith(Sink.foreach { h =>
      logger.info(s"Hashtag $h arrived at sink!")
      println(h) // Attach the Flow to a Sink that will finally print the hashtags
    })
}
