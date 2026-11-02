package seedu.address.storage;

import java.io.IOException;

/**
 * Indicates that another writer currently holds the student-data file lock.
 */
public class DataFileInUseException extends IOException {
    public DataFileInUseException() {
        super("Another writer is saving student data.");
    }
}
