import sbt._

object Dependencies {
  val logbackVersion = "1.2.3"
  val scalaCollectionCompatVersion = "2.3.2"
  val scalaLoggingVersion = "3.9.2"
  val scalatestVersion = "3.2.2"

  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion
  val scalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion

  object TestLibs {
    val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % Test
    val scalactic: ModuleID = "org.scalactic" %% "scalactic" % scalatestVersion % Test
  }
}
