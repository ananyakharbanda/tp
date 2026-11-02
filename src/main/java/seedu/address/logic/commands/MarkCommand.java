package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LABEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.attendance.AttendanceRecord;
import seedu.address.model.label.Label;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentId;

/**
 * Records that a student attended a tutorial group on a specified date.
 */
public class MarkCommand extends Command {

    public static final String COMMAND_WORD = "mark";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Usage: mark i/STUDENT_ID "
            + "d/YYYY-MM-DD l/GROUP";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Marks a student's attendance.\n"
            + "Parameters: " + PREFIX_STUDENT_ID + "STUDENT_ID " + PREFIX_DATE + "YYYY-MM-DD "
            + PREFIX_LABEL + "GROUP\n"
            + "Example: mark i/A0123456B d/2026-10-20 l/Tutorial 1";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student with ID %s is not in the records.";
    public static final String MESSAGE_GROUP_NOT_FOUND = "Student %1$s does not have group label \"%2$s\".";
    public static final String MESSAGE_ALREADY_MARKED = "Student %1$s is already marked present for %2$s on %3$s.";
    public static final String MESSAGE_SUCCESS = "Marked student %1$s present for %2$s on %3$s.";
    private static final Logger logger = LogsCenter.getLogger(MarkCommand.class);

    private final StudentId studentId;
    private final LocalDate date;
    private final Label group;

    /**
     * Creates a command to mark one student's attendance for a date and group.
     *
     * @param studentId The student's non-null ID.
     * @param date The non-null attendance date.
     * @param group The non-null group label.
     */
    public MarkCommand(StudentId studentId, LocalDate date, Label group) {
        requireAllNonNull(studentId, date, group);
        this.studentId = studentId;
        this.date = date;
        this.group = group;
    }

    @Override
    public boolean isModifyingData() {
        return true;
    }

    /**
     * {@inheritDoc}
     * Checks the complete student list and updates one student. Saving is handled by {@code LogicManager}.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        Objects.requireNonNull(model);
        Person student = findStudent(model);
        assert student != null;
        Label matchedGroup = findStudentGroup(student);
        assert matchedGroup != null;
        AttendanceRecord record = new AttendanceRecord(date, matchedGroup);
        if (student.getAttendance().contains(record)) {
            logger.warning("Mark rejected: attendance already recorded.");
            throw new CommandException(String.format(MESSAGE_ALREADY_MARKED, studentId, matchedGroup.labelName, date));
        }

        Set<AttendanceRecord> updatedAttendance = new HashSet<>(student.getAttendance());
        updatedAttendance.add(record);
        Person updatedStudent = new Person(student.getName(), student.getStudentId(), student.getEmail(),
                student.getLabels(), student.getRemark(), updatedAttendance);
        model.setPerson(student, updatedStudent);
        logger.info("Marked attendance.");
        return CommandResult.updatingAttendance(
                String.format(MESSAGE_SUCCESS, studentId, matchedGroup.labelName, date));
    }

    private Person findStudent(Model model) throws CommandException {
        assert model != null;
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("Mark rejected: student not found.");
                    return new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentId));
                });
    }

    private Label findStudentGroup(Person student) throws CommandException {
        assert student != null;
        return student.getLabels().stream()
                .filter(label -> label.equalsIgnoreCase(group))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("Mark rejected: group not assigned to student.");
                    return new CommandException(String.format(MESSAGE_GROUP_NOT_FOUND,
                            studentId, group.labelName));
                });
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof MarkCommand otherCommand)) {
            return false;
        }
        return studentId.equals(otherCommand.studentId)
                && date.equals(otherCommand.date)
                && group.equals(otherCommand.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, date, group);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentId", studentId)
                .add("date", date)
                .add("group", group)
                .toString();
    }
}
