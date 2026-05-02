package ink.reactor.launcher.logger

import ink.reactor.kernel.Reactor
import ink.reactor.kernel.logger.Logger
import ink.reactor.launcher.logger.console.ConsoleAppender
import ink.reactor.launcher.logger.file.FileAppender
import ink.reactor.launcher.logger.file.LogCompressor
import ink.reactor.launcher.logger.type.NoOpLogger
import ink.reactor.launcher.logger.type.ReactorLogger
import ink.reactor.microkernel.logger.JavaLoggerFormatter
import ink.reactor.sdk.config.ConfigService
import java.io.IOException
import java.io.PrintWriter
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.format.DateTimeFormatter
import kotlin.io.path.exists
import kotlin.io.path.fileSize

class LoggersLoader(
    private val consoleWriter: PrintWriter
) {

    fun load(configService: ConfigService): Logger {
        val config = LoggerConfig(configService.createIfAbsentAndLoad("logger"));
        if (!config.enable) {
            return NoOpLogger();
        }

        val ringBuffer = LogRingBuffer(2048)
        val consoleAppender = loadConsoleLogger(config.console)
        val fileAppender = loadFileLogger(config.logs)
        val autoFlush = config.logs.autoFlush

        val prefix = config.prefix
        val loggerFormatter = LogFormatter(DateTimeFormatter.ofPattern(prefix.dateFormatter))

        val processor = LogProcessorThread(
            ringBuffer,
            autoFlush.interval.inWholeSeconds,
            consoleAppender,
            fileAppender,
            loggerFormatter
        )

        processor.start()
        Reactor.addStopTask{
            processor.shutdown()
            try {
                processor.join(5000)
            } catch (_: InterruptedException) {}
        }

        return ReactorLogger(
            JavaLoggerFormatter(),
            ringBuffer,
            prefix.debug,
            prefix.log,
            prefix.info,
            prefix.warn,
            prefix.error
        )
    }

    private fun loadConsoleLogger(console: ConsoleConfig): ConsoleAppender? {
        if (!console.enable) {
            return null;
        }
        val styles = console.styles
        return ConsoleAppender(
            consoleWriter,
            console.levels,
            styles.debug,
            styles.log,
            styles.info,
            styles.warn,
            styles.error,
        )
    }

    private fun loadFileLogger(logs: FileLogsConfig): FileAppender? {
        if (!logs.enable) {
            return null
        }

        val path = Path.of("${logs.logsFolder}/latest.log")

        compressOldLog(path, logs.compression)
        val channel = createLogChannel(path) ?: return null

        val fileAppender = FileAppender(logs.maxFileSize, logs.bufferSize, channel)
        return fileAppender
    }

    private fun compressOldLog(path: Path, compression: Boolean) {
        try {
            if (path.exists() && path.fileSize() > 0) {
                LogCompressor.compress(path, compression)
            }
        } catch (e: IOException) {
            System.err.println("Can't compress old log file")
            e.printStackTrace(System.err)
        }
    }

    private fun createLogChannel(path: Path): FileChannel? {
        return try {
            path.parent?.let { Files.createDirectories(it) }
            FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        } catch (e: IOException) {
            System.err.println("Can't start file logger")
            e.printStackTrace(System.err)
            null
        }
    }
}
