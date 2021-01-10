resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

addSbtPlugin("com.rallyhealth.sbt" %% "sbt-shading" % "0.0.1-4-6ed9c71-SNAPSHOT")
addSbtPlugin("com.rallyhealth.sbt" %% "sbt-git-versioning" % "1.4.0")
addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.6.1")
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.7.0")
addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.12")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
