package app.main

import app.infra.MysqlClient
import com.twitter.util.Await
import org.slf4j.{Logger, LoggerFactory}
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.logging.{LogAnnotation, LogFormat, console, consoleJson}
import zio.{LogLevel, Runtime, Trace, ZIO, ZIOAppDefault, ZIOAspect, durationInt}

import java.util.UUID

object Main extends ZIOAppDefault {

   val slf4jLogger: Logger = LoggerFactory.getLogger("SLF4J LOGGER")

  private val userLogAnnotation: LogAnnotation[UUID] = LogAnnotation[UUID]("user", (_, i) => i, _.toString)
  private val simpleAnno = LogAnnotation[String]("TEST", _ + _, identity)


  val location: LogFormat =
    LogFormat.make { (builder, trace, _, _, _, _, _, _, _) =>
      trace match {
        case Trace(loc, _, line) => builder.appendText(s"$loc:$line")
        case _                 => builder.appendText("not-available")
      }
    }

//  private val logger =
//    Runtime.removeDefaultLoggers >>>
//    consoleJson(
//      LogFormat.default +
//        LogFormat.label("loc", location) +
//        LogFormat.spans +
//        LogFormat.annotations +
//        LogFormat.logAnnotations,
//      LogLevel.Trace) >>>
//    Slf4jBridge.initialize

  private val textLogger =
    Runtime.removeDefaultLoggers >>>
    console(
      LogFormat.default +
        LogFormat.label("loc", location) +
        LogFormat.spans +
        LogFormat.annotations +
        LogFormat.logAnnotations,
      LogLevel.Trace) >>>
    Slf4jBridge.initialize

  override def run: ZIO[Any, Any, Unit] = {
    val program = for {
      _ <- ZIO.logInfo("Start application") @@ userLogAnnotation(UUID.randomUUID())

      client <- ZIO.attempt(MysqlClient.init())
//      res <- ZIO.succeed(Await.result(client.select("select 1;")(_.toString())))
      _ <- ZIO.succeed(slf4jLogger.debug("TEST {}!", "TEST"))
      _ <- ZIO.logInfo("Process application") @@ simpleAnno("ANNO")
//      _ <- ZIO.fail(new Exception("DIE!"))
      _ <- ZIO.logDebug("Stop application") @@ ZIOAspect.annotated("GENERIC", "TEST")
      _ <- ZIO.sleep(1000.millis)
    } yield ()
    ZIO.logSpan("my app") {
      program.provide(textLogger)
    }
  }



}
