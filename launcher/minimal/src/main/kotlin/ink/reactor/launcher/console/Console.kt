package codes.reactor.launcher.console

import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.MaskingCallback
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import java.io.IOException

class Console(
    val terminal: Terminal,
    val reader: LineReader
) {
    private var run = true

    fun run() {
        while (run) {
            val line: String
            try {
                line = reader.readLine("> ", null, null as MaskingCallback?, null).trim { it <= ' ' }
            } catch (_: UserInterruptException) {
                stop()
                break
            } catch (_: EndOfFileException) {
                stop()
                break
            }

            if (line == "stop") {
                println("Stopping server...")
                stop()
                break
            }

            println("Execute: $line")
        }
    }

    fun stop() {
        if (!run) {
            return
        }

        try {
            terminal.close()
        } catch (e: IOException) {
            System.err.println("Error trying to stop the terminal")
            e.printStackTrace(System.err)
        } finally {
            run = false
            Runtime.getRuntime().exit(0)
        }
    }
}
