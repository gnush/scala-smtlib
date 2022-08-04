ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.12"
ThisBuild / libraryDependencies +="org.scalatest" %% "scalatest" % "3.2.12" % "test"

lazy val root = (project in file("."))
  .settings(
    name := "scala-smtlib"
  )
