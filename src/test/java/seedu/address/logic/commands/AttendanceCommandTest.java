package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.AttendanceRecord;
import seedu.address.model.attendance.AttendanceReport;
import seedu.address.model.label.Label;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AttendanceCommandTest {

    private static final LocalDate DATE = LocalDate.parse("2026-10-20");
    private static final Label GROUP = new Label("Tutorial 1");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_groupMembers_showsReportIncludingFilteredStudents() {
        // EP: group has present and absent current members, including members hidden by a filter
        Person present = new PersonBuilder(ALICE).withLabels("Tutorial 1")
                .withAttendance(new AttendanceRecord(DATE, GROUP)).build();
        Person absent = new PersonBuilder(BENSON).withLabels("Tutorial 1").build();
        model.setPerson(ALICE, present);
        model.setPerson(BENSON, absent);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Carl")));
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Carl")));

        Label requestedGroup = new Label("tutorial 1");
        AttendanceReport report = AttendanceReport.of(model.getAddressBook().getPersonList(), DATE,
                requestedGroup);
        assertCommandSuccess(new AttendanceCommand(DATE, requestedGroup), model,
                CommandResult.showingAttendance("Attendance for tutorial 1 on 2026-10-20: 1 present, 1 absent.",
                        report), expected);
    }

    @Test
    public void execute_noMembers_showsEmptyReport() {
        // EP: requested group has zero current members
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        AttendanceReport report = AttendanceReport.of(List.of(), DATE, GROUP);

        assertCommandSuccess(new AttendanceCommand(DATE, GROUP), model,
                CommandResult.showingAttendance("Attendance for tutorial 1 on 2026-10-20: 0 present, 0 absent.",
                        report), expected);
    }

    @Test
    public void isModifyingData_returnsFalse() {
        assertFalse(new AttendanceCommand(DATE, GROUP).isModifyingData());
    }

    @Test
    public void equals() {
        AttendanceCommand command = new AttendanceCommand(DATE, GROUP);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new AttendanceCommand(DATE, new Label("Tutorial 1"))));
        assertFalse(command.equals(new AttendanceCommand(DATE.plusDays(1), GROUP)));
        assertFalse(command.equals(new AttendanceCommand(DATE, new Label("Tutorial 2"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(GROUP));
        assertEquals(command.hashCode(), new AttendanceCommand(DATE, new Label("Tutorial 1")).hashCode());
    }

    @Test
    public void toStringMethod() {
        AttendanceCommand command = new AttendanceCommand(DATE, GROUP);
        String expected = AttendanceCommand.class.getCanonicalName() + "{date=" + DATE + ", group=" + GROUP + "}";
        assertEquals(expected, command.toString());
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        // EP: each required report field is absent
        assertThrows(NullPointerException.class, () -> new AttendanceCommand(null, GROUP));
        assertThrows(NullPointerException.class, () -> new AttendanceCommand(DATE, null));
    }
}
