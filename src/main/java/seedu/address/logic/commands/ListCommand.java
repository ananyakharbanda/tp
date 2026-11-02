package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all students in TeachAssist.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = "Usage: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Listed all students.";
    public static final String MESSAGE_NO_STUDENTS_FOUND = "No students found.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String resultMessage = model.getFilteredPersonList().isEmpty()
                ? MESSAGE_NO_STUDENTS_FOUND
                : MESSAGE_SUCCESS;
        return new CommandResult(resultMessage);
    }
}
