package codes.reactor.sdk.logger

import codes.reactor.kernel.logger.LoggerFormatter

object DummyLoggerFormatter : LoggerFormatter {
    override fun format(text: String, vararg objects: Any?): String {
        return text
    }
}
