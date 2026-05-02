package ink.reactor.launcher.logger.type

import ink.reactor.launcher.logger.LoggerLevels
import ink.reactor.launcher.logger.file.FileWriter

class FileLogger(
    private val loggerLevels: LoggerLevels,
    private val fileWriter: FileWriter
) {

    fun debug(message: String) {
        if (loggerLevels.debug) {
            fileWriter.write(message)
        }
    }

    fun log(message: String) {
        if (loggerLevels.log) {
            fileWriter.write(message)
        }
    }

    fun info(message: String) {
        if (loggerLevels.info) {
            fileWriter.write(message)
        }
    }

    fun warn(message: String) {
        if (loggerLevels.warn) {
            fileWriter.write(message)
        }
    }

    fun error(message: String) {
        if (loggerLevels.error) {
            fileWriter.write(message)
        }
    }

    fun error(message: String, throwable: Throwable) {
        if (loggerLevels.error) {
            fileWriter.write(message)
            fileWriter.write(('\n'.toString() + throwable.toString()))

            for (element in throwable.stackTrace) {
                fileWriter.write(("\n\tat $element"))
            }

            var cause = throwable.cause
            while (cause != null) {
                fileWriter.write(("\nCaused by: $cause"))
                for (element in cause.stackTrace) {
                    fileWriter.write(("\n\tat $element"))
                }
                cause = cause.cause
            }
        }
    }
}
