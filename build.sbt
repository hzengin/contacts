name := """contacts"""

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23"


fork in run := true
