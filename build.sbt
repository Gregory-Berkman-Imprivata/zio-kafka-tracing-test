ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val zioVersion = "1.0.16"
lazy val zioKafkaVersion = "0.17.5"
lazy val zioOpenTelemetryVersion = "1.0.0"
lazy val openTelemetryVersion = "1.11.0"

lazy val all = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-opentelemetry" % zioOpenTelemetryVersion,
  "dev.zio" %% "zio-kafka" % zioKafkaVersion,
  "io.opentelemetry" % "opentelemetry-api" % openTelemetryVersion,
  "io.opentelemetry" % "opentelemetry-sdk" % openTelemetryVersion,
  "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % openTelemetryVersion)

lazy val consumer = (project in file("consumer"))
  .settings(
    name := "consumer",
    mainClass := Some("consumer.Main"),
    libraryDependencies ++= all)

lazy val producer = (project in file("producer"))
  .settings(
    name := "producer",
    mainClass := Some("producer.Main"),
    libraryDependencies ++= all)

lazy val root = project
  .aggregate(consumer, producer)
  .in(file("."))
  .settings(name := "zio-kafka-tracing-test-app")

