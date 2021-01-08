import sbt._

object Dependencies {

  val scalatestVersion = "3.2.2"
  val scalaLoggingVersion = "3.9.2"

  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % scalatestVersion
  val scalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion

  object TestLibs {
    val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % Test
  }
}
