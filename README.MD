
[ZIO2 migration guide](https://zio.dev/guides/migrate/zio-2.x-migration-guide/)
[ZioLogging](https://zio.github.io/zio-logging/docs/overview/overview_index.html)

[x] Log location (filename, line etc.)
with help of custom formatter:
```scala
  val location: LogFormat =
    LogFormat.make { (builder, trace, _, _, _, _, _, _, _) =>
      trace match {
        case Trace(loc, _, line) => builder.appendText(s"$loc:$line")
        case _                 => builder.appendText("not-available")
      }
    }


  private val logger =
    Runtime.removeDefaultLoggers >>> consoleJson(LogFormat.default + LogFormat.label("location", location))

```
[x] Easy to add tracing to logs
             
```scala
    ZIO.logSpan("my app") {
      program.provide(logger)
    }

```
```json lines
{"my app":"1956ms", "timestamp":"2022-09-20T09:55:30.95284+04:00 ","level":"DEBUG","thread":"zio-fiber-6","message":"Stop application","loc":"app.main.Main.run.program:45"}
```

[x] Try forward all slf4j compatible logs (from libraries) to zio logging via `zio-logging-slf4j-bridge`


slf4j + ziologging works well (see MainSlf4jZio):
```json lines
{"timestamp":"2022-09-19T10:37:05.817422+04:00","level":"INFO","thread":"zio-fiber-6","message":"Start application","loc":"app.main.Main.run.program:40","user":"7d817d89-6438-452c-a570-baf913de6718"}
{"@timestamp":"2022-09-19T10:53:24.592+04:00","@version":"1","message":"-Djava.net.preferIPv4Stack: false","logger_name":"io.netty.util.NetUtil","thread_name":"ZScheduler-Worker-11","level":"DEBUG","level_value":10000}
{"@timestamp":"2022-09-19T10:53:24.600+04:00","@version":"1","message":"-Djava.net.preferIPv6Addresses: false","logger_name":"io.netty.util.NetUtil","thread_name":"ZScheduler-Worker-11","level":"DEBUG","level_value":10000}
{"@timestamp":"2022-09-19T10:53:24.604+04:00","@version":"1","message":"/proc/sys/net/core/somaxconn: 4096","logger_name":"io.netty.util.NetUtil","thread_name":"ZScheduler-Worker-11","level":"DEBUG","level_value":10000}
{"@timestamp":"2022-09-19T10:37:07.539+04:00","@version":"1","message":"TEST ERROR!","logger_name":"SLF4J LOGGER","thread_name":"ZScheduler-Worker-10","level":"ERROR","level_value":40000}
{"timestamp":"2022-09-19T10:37:07.550574+04:00","level":"INFO","thread":"zio-fiber-6","message":"Process application","loc":"app.main.Main.run.program:43","TEST":"ANNO"}
{"timestamp":"2022-09-19T10:37:07.560861+04:00","level":"INFO","thread":"zio-fiber-6","message":"Stop application","loc":"app.main.Main.run.program:45"}

```
Forwarding SLF4J logs to ZioLogger works properly

```scala
 object MainSlf4jBridge extends ZIOAppDefault {

   val slf4jLogger: Logger = LoggerFactory.getLogger("SLF4J LOGGER")

  private val logger =
    Runtime.removeDefaultLoggers >>> consoleJson(LogFormat.default + LogFormat.spans + LogFormat.annotations, LogLevel.Debug) >>> Slf4jBridge.initialize

  override def run: ZIO[Any, Any, Unit] = {
    val program = for {
      _ <- ZIO.logInfo("Start application")
      _ <- ZIO.succeed(slf4jLogger.debug("TEST {}!", "DEBUG"))
      _ <- ZIO.succeed(slf4jLogger.error("TEST {}!", "ERROR"))
      _ <- ZIO.logDebug("Stop application")
    } yield ()
    ZIO.logSpan("my app") {
      program.provide(logger)
    }
  }
}

```

```json lines
{"timestamp":"2022-09-20T11:09:14.202431+04:00","level":"INFO","thread":"zio-fiber-6","message":"Start application","my app":"172ms"}
{"timestamp":"2022-09-20T11:09:14.233889+04:00","level":"DEBUG","thread":"zio-fiber-7","message":"TEST DEBUG!","SLF4J LOGGER":"5ms","my app":"183ms"}
{"timestamp":"2022-09-20T11:09:14.243303+04:00","level":"ERROR","thread":"zio-fiber-8","message":"TEST ERROR!","SLF4J LOGGER":"4ms","my app":"190ms"}
{"timestamp":"2022-09-20T11:09:14.247402+04:00","level":"DEBUG","thread":"zio-fiber-6","message":"Stop application","my app":"194ms"}

```

**Problem:**
@pkotula when I use Slf4jBridge.initialize I see all SLF4J logs created by my logger, created like this: val slf4jLogger: Logger = LoggerFactory.getLogger("SLF4J LOGGER")). But I can see any logs from external libraries (for example Netty).
If I switch slf4j backend from zio-logging-slf4j-bridge to logback-classic I see Netty logs.
https://discord.com/channels/629491597070827530/629498664313815040/1021753977362194532

**Answer:**  hi, do not have instant answer, I know that some other people mentioned similar problems before 
(https://discordapp.com/channels/629491597070827530/629498664313815040/1014180847361986653),  personally I did not used 
slf4j-bridge in some "complex" application, but they may be initialisation issues (slf4j i think using static instances,
to access specific logger binder implementations)

- what is going on with MDC context while converting, does json logs have all fields from MDC context? 
             
[x] Try forward ZioLogging to Slf4j () via `zio-logging-slf4j` - DECLINED


Cons:
- ~~annotion should be predifined (no arbitrary json fiedls in logs)~~ - **FIXED with LogFormat.logAnnotations**
- ~~some slf4j messages don't forwarded to zioLogging~~
- die messages is being written in text format
- logs can't be filtered by anything except log lvl
- recompile code is required for changing log lvl

Pros:
- good, default Api (except annotation)
- don't need to explain for newcommers and don't need to support
- no deps in case of json logs (now Jackson, logstash etc.)
- it's possible to override log lvl in library
- integrated with tracing (add spans to log ZIO.logSpan("get-profile-pic
- 

Notes:

1. Zio can filter log only by level (filtering by package name\ logger name in not implemented yet, see https://github.com/zio/zio-logging/pull/512)
2. Change log level is possible only on compile time via `consoleJson(..., LogLevel.Debug)`
