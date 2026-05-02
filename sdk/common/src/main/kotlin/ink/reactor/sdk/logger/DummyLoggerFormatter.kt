package ink.reactor.sdk.logger

import ink.reactor.kernel.logger.LoggerFormatter

object DummyLoggerFormatter : LoggerFormatter {
    override fun format(text: String, vararg objects: Any?): String {
        return text
    }
}
