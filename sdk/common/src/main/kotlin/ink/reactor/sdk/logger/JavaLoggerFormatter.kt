package codes.reactor.sdk.logger

import codes.reactor.kernel.logger.LoggerFormatter
import java.util.*

class JavaLoggerFormatter : LoggerFormatter {
    private val formatter: Formatter

    constructor(formatter: Formatter) {
        this.formatter = formatter
    }

    constructor() {
        this.formatter = Formatter()
    }

    override fun format(text: String, vararg objects: Any?): String {
        return if (objects.isEmpty()) text else formatter.format(text, *objects).toString()
    }
}
