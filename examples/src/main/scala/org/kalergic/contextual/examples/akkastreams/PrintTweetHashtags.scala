package org.kalergic.contextual.examples.akkastreams

import com.typesafe.scalalogging.StrictLogging

object PrintTweetHashtags extends App with StrictLogging {

  import akka.NotUsed
  import akka.actor.ActorSystem
  import akka.stream.scaladsl._
  val akkaTag = Hashtag("#akka")

  val tweets: Source[Tweet, NotUsed] = Source(
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

  implicit val system: ActorSystem = ActorSystem(
    name = "reactive-tweets",
    defaultExecutionContext = Some(scala.concurrent.ExecutionContext.Implicits.global)
  )

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
