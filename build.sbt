enablePlugins(JavaAppPackaging)

name := "akka-http-social-network"
organization := "matlux.net"
version := "0.1"
scalaVersion := "2.12.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
assemblyJarName in assembly := "akka-http-social-network.jar"

mainClass in Compile := Some("net.matlux.SocialNetworkServer")

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
    "org.typelevel" %% "cats" % "0.9.0",

    //java stuff
    "org.jolokia" % "jolokia-jvm" % "1.3.7" classifier "agent",

    "org.scalatest" %% "scalatest" % scalaTestV % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test

  )
}

Revolver.settings
