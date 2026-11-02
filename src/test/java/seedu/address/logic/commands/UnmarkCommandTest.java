package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
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
import seedu.address.model.label.Label;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentId;
import seedu.address.testutil.PersonBuilder;

public class UnmarkCommandTest {

    private static final LocalDate DATE = LocalDate.parse("2026-10-20");
    private static final Label GROUP = new Label("Tutorial 1");
    private static final AttendanceRecord RECORD = new AttendanceRecord(DATE, GROUP);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_hiddenStudent_removesOnlyMatchingAttendanceAndPreservesFilter() {
        // EP: hidden student with the requested record; other dates and groups remain unchanged
        AttendanceRecord differentDate = new AttendanceRecord(DATE.plusDays(1), GROUP);
        AttendanceRecord differentGroup = new AttendanceRecord(DATE, new Label("Tutorial 2"));
        Person student = new PersonBuilder(ALICE).withLabels("Tutorial 1", "Tutorial 2")
                .withRemark("Needs help with recursion").withAttendance(RECORD, differentDate, differentGroup).build();
        model.setPerson(ALICE, student);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        Person unmarked = new PersonBuilder(student).withAttendance(differentDate, differentGroup).build();
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        expected.setPerson(student, unmarked);

        assertCommandSuccess(new UnmarkCommand(new StudentId("a1234567b"), DATE, new Label("tutorial 1")), model,
                CommandResult.updatingAttendance("Unmarked student A1234567B for tutorial 1 on 2026-10-20."), expected);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_removedGroupLabel_removesHistoricalAttendance() {
        // EP: historical attendance remains removable after current group membership is removed
        Person student = new PersonBuilder(ALICE).withLabels().withAttendance(RECORD).build();
        model.setPerson(ALICE, student);
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.setPerson(student, new PersonBuilder(student).withAttendance().build());

        assertCommandSuccess(new UnmarkCommand(student.getStudentId(), DATE, new Label("TUTORIAL 1")), model,
                CommandResult.updatingAttendance("Unmarked student A1234567B for tutorial 1 on 2026-10-20."), expected);
    }

    @Test
    public void execute_missingStudent_preservesRecords() {
        // EP: unknown student ID leaves the address book and active filter unchanged
        assertCommandFailure(new UnmarkCommand(new StudentId("Z9999999Z"), DATE, GROUP), model,
                "Student with ID Z9999999Z is not in the records.");
    }

    @Test
    public void execute_noAttendance_preservesRecords() {
        // EP: existing student has no attendance records; rejected removal leaves data unchanged
        Person student = new PersonBuilder(ALICE).withLabels(GROUP.labelName).withAttendance().build();
        model.setPerson(ALICE, student);

        assertCommandFailure(new UnmarkCommand(student.getStudentId(), DATE, GROUP), model,
                "Student A1234567B is not marked present for tutorial 1 on 2026-10-20.");
    }

    @Test
    public void execute_wrongDateOrGroup_preservesRecords() {
        // EP: record exists for only a different date or group; requested pair is absent
        Person student = new PersonBuilder(ALICE).withAttendance(RECORD).build();
        model.setPerson(ALICE, student);

        assertCommandFailure(new UnmarkCommand(student.getStudentId(), DATE.plusDays(1), GROUP), model,
                "Student A1234567B is not marked present for tutorial 1 on 2026-10-21.");
        assertCommandFailure(new UnmarkCommand(student.getStudentId(), DATE, new Label("Tutorial 2")), model,
                "Student A1234567B is not marked present for tutorial 2 on 2026-10-20.");
    }

    @Test
    public void execute_markThenUnmark_restoresOriginalStudent() throws Exception {
        // EP: mark followed by unmark of the same date/group restores the original state
        Person student = new PersonBuilder(ALICE).withLabels(GROUP.labelName).build();
        model.setPerson(ALICE, student);
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        new MarkCommand(student.getStudentId(), DATE, GROUP).execute(model);

        assertCommandSuccess(new UnmarkCommand(student.getStudentId(), DATE, GROUP), model,
                CommandResult.updatingAttendance("Unmarked student A1234567B for tutorial 1 on 2026-10-20."), expected);
        assertCommandFailure(new UnmarkCommand(student.getStudentId(), DATE, GROUP), model,
                "Student A1234567B is not marked present for tutorial 1 on 2026-10-20.");
    }

    @Test
    public void equals() {
        UnmarkCommand command = new UnmarkCommand(ALICE.getStudentId(), DATE, GROUP);
        UnmarkCommand copy = new UnmarkCommand(new StudentId("a1234567b"), DATE, GROUP);
        assertTrue(command.equals(command));
        assertTrue(command.equals(copy));
        assertEquals(command.hashCode(), copy.hashCode());
        assertFalse(command.equals(new UnmarkCommand(BENSON.getStudentId(), DATE, GROUP)));
        assertFalse(command.equals(new UnmarkCommand(ALICE.getStudentId(), DATE.plusDays(1), GROUP)));
        assertFalse(command.equals(new UnmarkCommand(ALICE.getStudentId(), DATE, new Label("Tutorial 2"))));
        assertFalse(command.equals(new MarkCommand(ALICE.getStudentId(), DATE, GROUP)));
        assertFalse(command.equals(null));
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        // EP: each required command field is absent
        assertThrows(NullPointerException.class, () -> new UnmarkCommand(null, DATE, GROUP));
        assertThrows(NullPointerException.class, () -> new UnmarkCommand(ALICE.getStudentId(), null, GROUP));
        assertThrows(NullPointerException.class, () -> new UnmarkCommand(ALICE.getStudentId(), DATE, null));
    }

    @Test
    public void toStringMethod() {
        UnmarkCommand command = new UnmarkCommand(ALICE.getStudentId(), DATE, GROUP);
        String expected = UnmarkCommand.class.getCanonicalName()
                + "{studentId=A1234567B, date=2026-10-20, group=[tutorial 1]}";
        assertEquals(expected, command.toString());
    }
}
