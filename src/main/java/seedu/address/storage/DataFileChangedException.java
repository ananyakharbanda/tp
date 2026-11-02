package seedu.address.storage;

import java.io.IOException;

/**
 * Indicates that saving would overwrite data changed since this storage instance last loaded or saved it.
 */
public class DataFileChangedException extends IOException {
    public DataFileChangedException() {
        super("Student data has changed since it was loaded or saved.");
    }
}
