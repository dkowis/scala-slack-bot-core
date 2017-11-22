name := "slack-scala-bot-core"

version := "0.3.0-SNAPSHOT"

scalaVersion := "2.12.4"

organization := "is.kow"

libraryDependencies ++= {
  val akkaHttpVersion = "10.0.10"
  val akkaVersion = "2.5.6"
  Seq(
    "org.mockito" % "mockito-core" % "1.10.19" % Test,

    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    //"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

    "joda-time" % "joda-time" % "2.7",
    "org.joda" % "joda-convert" % "1.7",
    "org.scalatest" %% "scalatest" % "3.0.4" % Test,

    "org.apache.logging.log4j" % "log4j-api" % "2.9.1",
    "org.apache.logging.log4j" % "log4j-core" % "2.9.1",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.9.1",
    "org.slf4j" % "slf4j-api" % "1.7.25",

    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.h2database" % "h2" % "1.4.186"
  )
}

publishMavenStyle := true

//TODO: probably figure out how to publish to maven central?
publishTo := Some(Resolver.file("file", new File("../mvn-repo")))

publishArtifact in Test := false

pomExtra := (
  <url>http://david.kow.is/</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>http://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:dkowis/scala-slack-bot-core.git</url>
      <connection>scm:git:git@github.com:dkowis/scala-slack-bot-core.git</connection>
    </scm>
    <developers>
      <developer>
        <id>dkowis</id>
        <name>David Kowis</name>
      </developer>
    </developers>)