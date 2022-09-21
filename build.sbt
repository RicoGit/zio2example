val ZioV = "2.0.2"

val Zio = "dev.zio" %% "zio" % ZioV
//val ZioTest = "dev.zio" %% "zio-test" % ZioV % Test
val ZioLogging = "dev.zio" %% "zio-logging" % "2.1.1"


ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"


lazy val root = (project in file("."))
  .settings(
    name := "zio2example",
    libraryDependencies ++= Seq(
      Zio,
//      ZioTest,
      ZioLogging,

      "com.twitter" %% "finagle-mysql" % "21.8.0",
      "org.slf4j" % "slf4j-api" % "1.7.36",
      "dev.zio" %% "zio-logging-slf4j-bridge" % "2.1.0",
//            "ch.qos.logback" % "logback-classic" % "1.2.11",
//      "net.logstash.logback" % "logstash-logback-encoder" % "6.4"
),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
