package seedu.address.model.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.attendance.AttendanceReport.AttendanceEntry;
import seedu.address.model.label.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AttendanceReportTest {

    private static final LocalDate DATE = LocalDate.parse("2026-10-20");
    private static final Label GROUP = new Label("Tutorial 1");
    private static final Label OTHER_GROUP = new Label("Tutorial 2");

    @Test
    public void of_currentMembers_reportsStatusInListOrder() {
        // EP: present and absent current members are included in supplied list order; non-members are excluded
        Person present = new PersonBuilder(CARL).withLabels("tutorial 1")
                .withAttendance(new AttendanceRecord(DATE, GROUP)).build();
        Person absent = new PersonBuilder(ALICE).withLabels("Tutorial 1", "Tutorial 2")
                .withAttendance(new AttendanceRecord(DATE, OTHER_GROUP),
                        new AttendanceRecord(DATE.plusDays(1), GROUP)).build();
        Person outsideGroup = new PersonBuilder(BENSON).withLabels("Tutorial 2")
                .withAttendance(new AttendanceRecord(DATE, OTHER_GROUP)).build();

        AttendanceReport report = AttendanceReport.of(List.of(present, outsideGroup, absent), DATE,
                new Label("TUTORIAL 1"));

        assertEquals(List.of(new AttendanceEntry(present, true), new AttendanceEntry(absent, false)),
                report.getEntries());
        assertEquals(1, report.getPresentCount());
        assertEquals(1, report.getAbsentCount());
        assertEquals(DATE, report.getDate());
        assertEquals(new Label("TUTORIAL 1"), report.getGroup());
    }

    @Test
    public void of_multipleGroupsOnOneDate_reportsEachGroupSeparately() {
        // EP: attendance for one group on a date does not make a member present in another group
        Person student = new PersonBuilder(ALICE).withLabels("Tutorial 1", "Tutorial 2")
                .withAttendance(new AttendanceRecord(DATE, OTHER_GROUP)).build();

        assertEquals(List.of(new AttendanceEntry(student, false)),
                AttendanceReport.of(List.of(student), DATE, GROUP).getEntries());
        assertEquals(List.of(new AttendanceEntry(student, true)),
                AttendanceReport.of(List.of(student), DATE, OTHER_GROUP).getEntries());
    }

    @Test
    public void of_nobodyMarked_reportsAllAbsent() {
        // EP: current members with no matching records are all absent
        Person first = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        Person second = new PersonBuilder(DANIEL).withLabels("Tutorial 1").build();

        AttendanceReport report = AttendanceReport.of(List.of(first, second), DATE, GROUP);

        assertEquals(0, report.getPresentCount());
        assertEquals(2, report.getAbsentCount());
    }

    @Test
    public void of_recordWithoutCurrentLabel_excludesStudent() {
        // EP: former group member with a historical record is excluded from the current-members report
        Person formerMember = new PersonBuilder(ALICE).withAttendance(new AttendanceRecord(DATE, GROUP)).build();

        AttendanceReport report = AttendanceReport.of(List.of(formerMember, BENSON), DATE, GROUP);

        assertEquals(List.of(), report.getEntries());
        assertEquals(0, report.getPresentCount());
        assertEquals(0, report.getAbsentCount());
    }

    @Test
    public void getEntries_modification_throwsUnsupportedOperationException() {
        // EP: report entries are immutable to callers
        Person member = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        AttendanceReport report = AttendanceReport.of(List.of(member), DATE, GROUP);
        assertThrows(UnsupportedOperationException.class, () -> report.getEntries().clear());
    }

    @Test
    public void of_nullArgument_throwsNullPointerException() {
        // EP: source students, date, group, or entry student is absent
        assertThrows(NullPointerException.class, () -> AttendanceReport.of(null, DATE, GROUP));
        assertThrows(NullPointerException.class, () -> AttendanceReport.of(List.of(), null, GROUP));
        assertThrows(NullPointerException.class, () -> AttendanceReport.of(List.of(), DATE, null));
        assertThrows(NullPointerException.class, () -> new AttendanceEntry(null, true));
    }

    @Test
    public void equals() {
        Person member = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        AttendanceReport report = AttendanceReport.of(List.of(member), DATE, GROUP);

        assertTrue(report.equals(report));
        assertTrue(report.equals(AttendanceReport.of(List.of(member), DATE, GROUP)));
        assertEquals(report.hashCode(), AttendanceReport.of(List.of(member), DATE, GROUP).hashCode());
        assertFalse(report.equals(AttendanceReport.of(List.of(member), DATE.plusDays(1), GROUP)));
        assertFalse(report.equals(AttendanceReport.of(List.of(), DATE, GROUP)));
        // EP: case variants normalize to the same report group
        AttendanceReport differentCase = AttendanceReport.of(List.of(member), DATE, new Label("TUTORIAL 1"));
        assertTrue(report.equals(differentCase));
        assertEquals(report.hashCode(), differentCase.hashCode());
        assertFalse(report.equals(null));
        assertFalse(report.equals(GROUP));
    }

    @Test
    public void toStringMethod() {
        Person member = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        AttendanceReport report = AttendanceReport.of(List.of(member), DATE, GROUP);
        String expected = AttendanceReport.class.getCanonicalName() + "{date=" + DATE + ", group=" + GROUP
                + ", entries=" + List.of(new AttendanceEntry(member, false)) + "}";
        assertEquals(expected, report.toString());
    }
}
