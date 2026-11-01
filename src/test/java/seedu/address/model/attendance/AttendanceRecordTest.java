package seedu.address.model.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.model.label.Label;

public class AttendanceRecordTest {

    private static final LocalDate DATE = LocalDate.parse("2026-10-20");

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: required record date or group is absent
        assertThrows(NullPointerException.class, () -> new AttendanceRecord(null, new Label("Tutorial 1")));
        // EP: null group
        assertThrows(NullPointerException.class, () -> new AttendanceRecord(DATE, null));
    }

    @Test
    public void equals_groupIgnoringCase_returnsTrue() {
        // EP: same date and group differing only by case denotes one attendance record
        AttendanceRecord record = new AttendanceRecord(DATE, new Label("Tutorial 1"));
        AttendanceRecord sameIgnoringCase = new AttendanceRecord(DATE, new Label("TUTORIAL 1"));

        // EP: same object
        assertTrue(record.equals(record));
        // EP: same date, same group name differing only in case
        assertTrue(record.equals(sameIgnoringCase));
        assertEquals(record.hashCode(), sameIgnoringCase.hashCode());

        // EP: different date
        assertFalse(record.equals(new AttendanceRecord(DATE.plusDays(1), new Label("Tutorial 1"))));
        // EP: different group
        assertFalse(record.equals(new AttendanceRecord(DATE, new Label("Tutorial 2"))));
        // EP: null
        assertFalse(record.equals(null));
        // EP: different type
        assertFalse(record.equals("some string"));
    }

    @Test
    public void getters_returnStoredValues() {
        // EP: valid AttendanceRecord
        AttendanceRecord record = new AttendanceRecord(DATE, new Label("Tutorial 1"));
        assertEquals(DATE, record.getDate());
        assertEquals(new Label("Tutorial 1"), record.getGroup());
        assertEquals("2026-10-20: tutorial 1", record.toString());
    }
}
