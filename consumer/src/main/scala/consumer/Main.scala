package consumer

import io.opentelemetry.api.GlobalOpenTelemetry
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.telemetry.opentelemetry.TracingSyntax.OpenTelemetryZioOps
import zio.{ExitCode, RIO, URIO, ZIO, ZLayer}
import zio.telemetry.opentelemetry._

object Main extends zio.App {

  val tracing = (ZLayer.succeed(
    GlobalOpenTelemetry.getTracerProvider
      .get("io.opentelemetry.api")) ++ Clock.any) >>> Tracing.live

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    consumer.root("consumer main").provideLayer(zio.ZEnv.live ++ tracing).exitCode
  }

  def consumer = {
    Consumer.consumeWith(
      ConsumerSettings(List("http://localhost:9092")).withGroupId("groupId"),
      Subscription.Topics(Set("topic")),
      Serde.string,
      Serde.string) { case (_, v) =>

      zio
        .console
        .putStrLn(s"consuming $v")
        .tapError(ex => zio.console.putStrLn(ex.getMessage))
        .orElse(ZIO.unit)
        .span(s"span $v")
    }
  }
}
