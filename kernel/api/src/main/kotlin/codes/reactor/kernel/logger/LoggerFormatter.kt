package codes.reactor.kernel.logger

interface LoggerFormatter {
    /**
     * Format a string replacing placeholders, example:
     * `format("hello %s", "world")`
     */
    fun format(text: String, vararg objects: Any?): String
}
