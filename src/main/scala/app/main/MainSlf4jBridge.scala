//package app.main
//
//import org.slf4j.{Logger, LoggerFactory}
//import zio.logging._
//import zio.logging.slf4j.bridge.Slf4jBridge
//import zio.{LogLevel, ZIO, ZIOAppDefault, Runtime}
//
//object MainSlf4jBridge extends ZIOAppDefault {
//
//   val slf4jLogger: Logger = LoggerFactory.getLogger("SLF4J LOGGER")
//
//  private val logger =
////    Slf4jBridge.initialize ++ Runtime.removeDefaultLoggers >>> console(LogFormat.colored, LogLevel.Debug)
//    Runtime.removeDefaultLoggers >>> consoleJson(LogFormat.default + LogFormat.spans + LogFormat.annotations, LogLevel.Debug) >>> Slf4jBridge.initialize
//
//  override def run: ZIO[Any, Any, Unit] = {
//    val program = for {
//      _ <- ZIO.logInfo("Start application")
//      _ <- ZIO.succeed(slf4jLogger.debug("TEST {}!", "DEBUG"))
//      _ <- ZIO.succeed(slf4jLogger.error("TEST {}!", "ERROR"))
//      _ <- ZIO.logDebug("Stop application")
//    } yield ()
//    ZIO.logSpan("my app") {
//      program.provide(logger)
//    }
//  }
//}
