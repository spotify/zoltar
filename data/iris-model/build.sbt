inThisBuild(scalaVersion := "2.11.12")

lazy val root = project
  .in(file("."))
  .settings(
    libraryDependencies := Seq(
      "com.spotify" %% "scio-core" % "0.5.7",
      "com.spotify" %% "scio-bigquery" % "0.5.7",
      "com.spotify" %% "scio-tensorflow" % "0.5.7",
      "com.spotify" %% "scio-test" % "0.5.7" % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    ),
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
    )
  )
