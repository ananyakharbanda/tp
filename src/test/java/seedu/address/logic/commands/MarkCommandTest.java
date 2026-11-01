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

public class MarkCommandTest {

    private static final LocalDate DATE = LocalDate.parse("2026-10-20");
    private static final Label GROUP = new Label("Tutorial 1");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_studentInGroup_addsAttendance() {
        // EP: existing student with a current group label, hidden by the active filter
        Person student = new PersonBuilder(ALICE).withLabels(GROUP.labelName).build();
        model.setPerson(ALICE, student);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        Person marked = new PersonBuilder(student).withAttendance(new AttendanceRecord(DATE, GROUP)).build();
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        expected.setPerson(student, marked);

        assertCommandSuccess(new MarkCommand(student.getStudentId(), DATE, new Label("tutorial 1")), model,
                CommandResult.updatingAttendance("Marked student A1234567B present for tutorial 1 on 2026-10-20."),
                expected);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_missingStudentOrGroup_preservesRecords() {
        // EP: unknown student ID leaves the address book and active filter unchanged
        assertCommandFailure(new MarkCommand(new StudentId("Z9999999Z"), DATE, GROUP), model,
                "Student with ID Z9999999Z is not in the records.");
        // EP: existing student without the requested current group label leaves data unchanged
        assertCommandFailure(new MarkCommand(ALICE.getStudentId(), DATE, GROUP), model,
                "Student A1234567B does not have group label \"tutorial 1\".");
    }

    @Test
    public void execute_duplicateDateAndGroup_rejectsRegardlessOfCase() {
        // EP: an existing date/group pair, matched case-insensitively, is not added twice
        AttendanceRecord record = new AttendanceRecord(DATE, GROUP);
        Person student = new PersonBuilder(ALICE).withLabels(GROUP.labelName).withAttendance(record).build();
        model.setPerson(ALICE, student);

        assertCommandFailure(new MarkCommand(student.getStudentId(), DATE, new Label("tutorial 1")), model,
                "Student A1234567B is already marked present for tutorial 1 on 2026-10-20.");
    }

    @Test
    public void equals() {
        MarkCommand command = new MarkCommand(ALICE.getStudentId(), DATE, GROUP);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new MarkCommand(new StudentId("a1234567b"), DATE, GROUP)));
        assertFalse(command.equals(new MarkCommand(BENSON.getStudentId(), DATE, GROUP)));
        assertFalse(command.equals(new MarkCommand(ALICE.getStudentId(), DATE.plusDays(1), GROUP)));
        assertFalse(command.equals(null));
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        // EP: each required command field is absent
        assertThrows(NullPointerException.class, () -> new MarkCommand(null, DATE, GROUP));
        assertThrows(NullPointerException.class, () -> new MarkCommand(ALICE.getStudentId(), null, GROUP));
        assertThrows(NullPointerException.class, () -> new MarkCommand(ALICE.getStudentId(), DATE, null));
    }
}
