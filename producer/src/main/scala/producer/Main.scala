package producer

import io.opentelemetry.api.GlobalOpenTelemetry
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.duration.durationInt
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.random.Random
import zio.telemetry.opentelemetry.Tracing
import zio.telemetry.opentelemetry.TracingSyntax.OpenTelemetryZioOps
import zio.{ExitCode, Schedule, URIO, ZIO, ZLayer, random}

object Main extends zio.App {

  val tracing = (ZLayer.succeed(
    GlobalOpenTelemetry.getTracerProvider
      .get("io.opentelemetry.api")) ++ Clock.any) >>> Tracing.live

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    producer.root("producer main").provideLayer(zio.ZEnv.live ++ tracing).exitCode
  }

  def producer: ZIO[Console with Blocking with Random with Clock, Throwable, Unit] = (for {
    id <- random.nextIntBounded(1000)
    _ <- zio.console.putStrLn(s"producing $id")
    _ <- Producer.produce("topic", s"key$id", s"value$id", Serde.string, Serde.string)
  } yield ()).repeat(Schedule.forever.addDelay(_ => 5.seconds)).unit
    .provideLayer {
      val producerLayer = (ZLayer.succeed(ProducerSettings(List("http://localhost:9092"))) ++ Blocking.any) >>> Producer.live
      producerLayer ++ Random.any ++ Clock.any ++ Console.any
    }
}
