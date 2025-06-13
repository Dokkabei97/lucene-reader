ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "reader-scala"
  )

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % "8.5.1",
  // Spark provided dependencies for running inside a cluster
  ("org.apache.spark" %% "spark-core" % "3.5.0" % Provided).cross(CrossVersion.for3Use2_13),
  ("org.apache.spark" %% "spark-sql"  % "3.5.0" % Provided).cross(CrossVersion.for3Use2_13)
)
