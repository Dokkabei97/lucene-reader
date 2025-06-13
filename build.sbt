ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "reader-scala"
  )

libraryDependencies += "org.apache.lucene" % "lucene-core" % "8.5.1"
