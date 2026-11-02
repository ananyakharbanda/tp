package seedu.address.testutil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import seedu.address.commons.core.LogsCenter;

/**
 * Captures formatted application diagnostics, including exceptions, at every logging level.
 * Restores the original logging configuration when closed.
 */
public class DiagnosticLogCapture implements AutoCloseable {
    private final List<String> messages = new CopyOnWriteArrayList<>();
    private final Logger logger = LogsCenter.getLogger(DiagnosticLogCapture.class).getParent();
    private final Level previousLevel = logger.getLevel();
    private final Handler handler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            messages.add(new SimpleFormatter().format(record));
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    };

    /**
     * Starts capturing diagnostics without replacing the application's existing handlers.
     */
    public DiagnosticLogCapture() {
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    public String getOutput() {
        return String.join("\n", messages);
    }

    @Override
    public void close() {
        logger.removeHandler(handler);
        logger.setLevel(previousLevel);
    }
}
