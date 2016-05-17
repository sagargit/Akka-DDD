name := "ProperDDDwithAkka"

version := "1.0"

lazy val `properdddwithakka` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.9",
  "com.typesafe.akka" % "akka-persistence-experimental_2.11" % "2.3.9",
  jdbc ,
  anorm ,
  cache ,
  ws )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  