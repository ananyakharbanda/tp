package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentId;

/**
 * Deletes a student identified by their normalised Student ID, regardless of the displayed filter.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes a student identified by their Student ID.\n"
            + "Parameters: i/STUDENT_ID\n"
            + "Example: " + COMMAND_WORD + " i/A0123456B";

    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Usage: delete i/STUDENT_ID";
    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted student %1$s: %2$s.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student with ID %s is not in the records.";
    private static final Logger logger = LogsCenter.getLogger(DeleteCommand.class);

    private final StudentId targetStudentId;

    /**
     * Creates a command to delete the student with the supplied Student ID.
     *
     * @param studentId The non-null, validated Student ID.
     * @throws NullPointerException if {@code studentId} is null.
     */
    public DeleteCommand(StudentId studentId) {
        requireNonNull(studentId);
        this.targetStudentId = studentId;
    }

    @Override
    public boolean isModifyingData() {
        return true;
    }

    /**
     * {@inheritDoc}
     * Finds the student in the complete record list and preserves the current displayed filter.
     * Saving and publishing changes to the live model are managed by {@code LogicManager}.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person targetStudent = findStudent(model);

        model.deletePerson(targetStudent);
        assert !model.hasPerson(targetStudent);
        logger.info("Deleted student.");
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS,
                targetStudent.getStudentId(), targetStudent.getName()));
    }

    private Person findStudent(Model model) throws CommandException {
        assert model != null;
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getStudentId().equals(targetStudentId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("Delete rejected: student not found.");
                    return new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, targetStudentId));
                });
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetStudentId.equals(otherDeleteCommand.targetStudentId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetStudentId", targetStudentId)
                .toString();
    }
}
