package codes.reactor.launcher.console

import codes.reactor.kernel.Reactor
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.IOException

object JLineConsole {
    fun createConsole(): Console? {
        val terminal: Terminal
        try {
            terminal = TerminalBuilder.builder()
                .system(true)
                .build()
        } catch (e: IOException) {
            System.err.println("Error on creating terminal")
            e.printStackTrace(System.err)
            return null
        }

        val reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .highlighter(ConsoleHighlighter())
            .completer(ConsoleCompleter())
            .build()

        val console = Console(terminal, reader)
        Reactor.addStopTask{console.stop()}

        return console
    }
}
