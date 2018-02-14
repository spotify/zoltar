/*
 * Copyright 2017 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import sbt._
import Keys._

val scioVersion = "0.5.0-alpha1"
val beamVersion = "2.2.0"
val slf4jVersion = "1.7.13"
val shapelessDatatypeVersion = "0.1.7"
val featranVersion = "0.1.16"

organization          := "com.spotify.data.example"
// Use semantic versioning http://semver.org/
version               := "0.1.0-SNAPSHOT"
scalaVersion          := "2.11.11"
scalacOptions         ++= Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked")
javacOptions          ++= Seq("-source", "1.8", "-target", "1.8")

// Repositories and dependencies
resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Concurrent Maven Repo" at "http://conjars.org/repo",
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

// In case avro/proto is used:
compileOrder := CompileOrder.JavaThenScala
publish := {}
publishLocal := {}
publishArtifact := false

description := "Feature spec for iris dataset"

libraryDependencies ++= Seq(
  "com.spotify" %% "scio-core" % scioVersion,
  "com.spotify" %% "scio-extra" % scioVersion,
  "com.spotify" %% "scio-tensorflow" % scioVersion,
  "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % beamVersion,
  "org.apache.beam" % "beam-runners-direct-java" % beamVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "org.scalanlp" %% "breeze" % "0.13.2",
  "com.spotify" %% "scio-test" % scioVersion % "test",
  "com.spotify" %% "featran-core" % featranVersion
)

// Required for typed BigQuery macros:
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

// Run Scala style check as part of (before) tests:
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
testScalastyle := org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle.in(Test).toTask("").value
(test in Test) := (test in Test) dependsOn testScalastyle
