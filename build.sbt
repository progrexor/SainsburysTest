name := "Sainsburys"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.3"

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")
