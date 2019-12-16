enablePlugins(JavaAppPackaging)

name := "akka-http-social-network"
organization := "matlux.net"
version := "0.1"
scalaVersion := "2.12.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
assemblyJarName in assembly := "akka-http-social-network.jar"

mainClass in Compile := Some("net.matlux.socialnetwork.Main")
// if your project uses multiple Scala versions, use this for cross building
addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)


libraryDependencies ++= {
  val akkaStreamVersion = "2.5.4"
  val akkaHttpVersion = "10.0.10"
  val scalaTestV = "3.0.4"

  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

    // Only when running against Akka 2.5 explicitly depend on akka-streams in same version as akka-actor
    "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion, // or whatever the latest version is
    "com.typesafe.akka" %% "akka-actor"  % akkaStreamVersion,
    "org.typelevel" %% "cats-effect" % "0.9",
    "org.typelevel" %% "cats-kernel" % "1.0.1",
    "org.typelevel" %% "cats-core" % "1.0.1",
    "org.typelevel" %% "cats-macros" % "1.0.1",
    "org.typelevel" %% "machinist" % "0.6.2",
    "co.fs2" %% "fs2-io" % "0.9.2",
    "co.fs2" %% "fs2-cats" % "0.5.0",

    //java stuff
    "org.jolokia" % "jolokia-jvm" % "1.3.7" classifier "agent",

    //logging
    "org.slf4j" % "slf4j-api"     % "1.7.25",
    "org.slf4j" % "slf4j-log4j12" % "1.7.25",

    "org.scalatest" %% "scalatest" % scalaTestV % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "org.specs2" %% "specs2-core" % "4.6.0" % Test

  )
}

Revolver.settings
