package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LABEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a student, identified by their normalised Student ID, to TeachAssist.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String COMMAND_PARAMETERS = PREFIX_NAME + "NAME "
            + PREFIX_STUDENT_ID + "STUDENT_ID "
            + PREFIX_EMAIL + "EMAIL "
            + "[" + PREFIX_REMARK + "REMARK] "
            + "[" + PREFIX_LABEL + "LABEL_NAME]";

    public static final String MESSAGE_INVALID_FORMAT =
            "Invalid command format. Usage: " + COMMAND_WORD + " " + COMMAND_PARAMETERS;
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a student to TeachAssist. "
            + "Parameters: " + COMMAND_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_STUDENT_ID + "A0123456B "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_REMARK + "Needs help with recursion "
            + PREFIX_LABEL + "Discrete Math Tutorial";

    public static final String MESSAGE_SUCCESS = "Added student %1$s: %2$s.";
    public static final String MESSAGE_DUPLICATE_PERSON = "Student ID %s already exists.";
    private static final String MESSAGE_REMARK = " Remark: %s";
    private static final Logger logger = LogsCenter.getLogger(AddCommand.class);

    private final Person toAdd;

    /**
     * Creates a command to add the specified student.
     *
     * @param person The non-null student with validated fields.
     * @throws NullPointerException if {@code person} is null.
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public boolean isModifyingData() {
        return true;
    }

    /**
     * {@inheritDoc}
     * Rejects a duplicate Student ID and reports the added student's ID, name, optional remark, and total count.
     * Saving and publishing to the live model are managed by {@code LogicManager}.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        assert toAdd != null : "Student to add must not be null";

        if (model.hasPerson(toAdd)) {
            logger.warning("Add rejected: duplicate student ID.");
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON, toAdd.getStudentId()));
        }

        model.addPerson(toAdd);
        assert model.hasPerson(toAdd) : "Model should contain the newly added student";
        logger.info("Added student.");
        return new CommandResult(formatSuccess(model.getAddressBook().getPersonList().size()));
    }

    private String formatSuccess(int studentCount) {
        String message = String.format(MESSAGE_SUCCESS, toAdd.getStudentId(), toAdd.getName());
        if (!toAdd.getRemark().value.isEmpty()) {
            message += String.format(MESSAGE_REMARK, toAdd.getRemark());
        }
        return message + "\n" + Messages.formatStudentsListed(studentCount);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
