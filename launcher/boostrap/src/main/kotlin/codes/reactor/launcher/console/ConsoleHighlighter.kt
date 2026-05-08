package codes.reactor.launcher.console

import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle

internal class ConsoleHighlighter : Highlighter {
    private val unknownCommand: AttributedStyle? = AttributedStyle()
        .foreground(AttributedStyle.RED, AttributedStyle.RED, AttributedStyle.RED)

    override fun highlight(reader: LineReader?, buffer: String): AttributedString {
        // TODO: Command highlighter
        if (!buffer.startsWith("stop")) {
            return AttributedString(buffer, unknownCommand)
        }
        return AttributedString(buffer)
    }
}
