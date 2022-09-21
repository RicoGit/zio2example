package app.main

import app.infra.MysqlClient
import org.slf4j.{Logger, LoggerFactory}
import zio.logging.{LogAnnotation, LogFormat, consoleJson}
import zio.{Runtime, Trace, ZIO, ZIOAppDefault, ZIOAspect}

import java.util.UUID

// Example when ZIOLogging and SLF4J+logback work independently
object MainSlf4jZio extends ZIOAppDefault {

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

  private val logger =
    Runtime.removeDefaultLoggers >>> consoleJson(
      LogFormat.default
        + LogFormat.label("loc", location)
        + LogFormat.annotation(simpleAnno)
        + LogFormat.annotation(userLogAnnotation)
    )

  override def run: ZIO[Any, Any, Unit] = {
    val program = for {
      _ <- ZIO.logInfo("Start application") @@ userLogAnnotation(UUID.randomUUID())
      _ <- ZIO.succeed(MysqlClient.init())
      _ <- ZIO.succeed(slf4jLogger.error("TEST {}!", "ERROR")) // json from slf4j
      _ <- ZIO.logInfo("Process application") @@ simpleAnno("ANNO")
      _ <- ZIO.logInfo("Stop application") @@ ZIOAspect.annotated("GENERIC", "TEST")
    } yield ()
      program.provide(logger)
  }



}
