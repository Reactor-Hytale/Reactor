package codes.reactor.boostrap.logger;

import java.io.IOException;

public interface LogAppender extends AutoCloseable {

    void append(MutableLogEntry entry) throws IOException;

    void flush() throws IOException;
}
