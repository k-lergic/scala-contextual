import sbt._
import Dependencies._

name := "scala-contextual"

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization := "org.kalergic.contextual"

// Versions!
val scala2_12 = "2.12.12"
val scala2_13 = "2.13.4"

ThisBuild / gitVersioningSnapshotLowerBound := "0.0.1"

ThisBuild / scalaVersion := scala2_13

ThisBuild / publishMavenStyle := false

ThisBuild / resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

publishArtifact := false

lazy val core = (projectMatrix in file("core"))
  .enablePlugins(SemVerPlugin)
  .enablePlugins(ShadingPlugin)
  .settings(
    // This is required because sbt-project-matrix (for cross-compiling) has its own idea of what the project name is;
    // we need to adjust for that.
    name := shadingNameShader.value(s"scala-contextual-${name.value}"),
    semVerEnforceAfterVersion := Some("1.0.0"),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Xfatal-warnings",
      "-Ywarn-dead-code"
    ),
    coverageEnabled := true,
    libraryDependencies ++= Seq(
      scalaCollectionCompat,
      scalactic,
      scalaLogging,
      TestLibs.scalatest
    ),
  ).jvmPlatform(scalaVersions = Seq(scala2_12, scala2_13))
