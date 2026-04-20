package ink.reactor.launcher.logger

import ink.reactor.kernel.Reactor
import ink.reactor.kernel.logger.Logger
import ink.reactor.launcher.logger.file.LogCompressor
import ink.reactor.launcher.logger.file.FileLogProcessorThread
import ink.reactor.launcher.logger.file.FileWriter
import ink.reactor.launcher.logger.type.ConsoleLogger
import ink.reactor.launcher.logger.type.FileLogger
import ink.reactor.launcher.logger.type.NoneLogger
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
            return NoneLogger();
        }

        val prefix = config.prefix
        return ReactorLogger(
            JavaLoggerFormatter(),
            loadConsoleLogger(config.console),
            loadFileLogger(config.logs),
            prefix.debug,
            prefix.log,
            prefix.info,
            prefix.warn,
            prefix.error,
            DateTimeFormatter.ofPattern(prefix.dateFormatter)
        )
    }

    private fun loadConsoleLogger(console: ConsoleConfig): ConsoleLogger? {
        if (!console.enable) {
            return null;
        }
        val styles = console.styles
        return ConsoleLogger(
            consoleWriter,
            console.levels,
            styles.debug,
            styles.log,
            styles.info,
            styles.warn,
            styles.error,
        )
    }

    private fun loadFileLogger(logs: FileLogsConfig): FileLogger? {
        if (!logs.enable) {
            return null
        }

        val path = Path.of("${logs.logsFolder}/latest.log")

        compressOldLog(path, logs.compression)
        val channel = createLogChannel(path) ?: return null

        val fileWriter = FileWriter(logs.maxFileSize, logs.bufferSize, channel)
        val autoFlush = logs.autoFlush

        val processor = FileLogProcessorThread(fileWriter, autoFlush.interval.inWholeSeconds)
        processor.start()
        Reactor.addStopTask{
            processor.shutdown()
            try {
                processor.join(5000)
            } catch (_: InterruptedException) {}
            fileWriter.close()
        }

        return FileLogger(logs.levels, fileWriter)
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
