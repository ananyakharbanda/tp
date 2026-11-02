package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds students whose selected field matches the search predicate, including complete group-label matches.
 * Keyword matching is case insensitive.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds students by name, student ID, email, remark, "
            + "or group label (case-insensitive). Names, IDs, emails, and remarks support partial keyword matches; "
            + "labels must match the complete label name.\n"
            + "Parameters: n/KEYWORD [MORE_KEYWORDS]... or i/KEYWORD [MORE_KEYWORDS]... "
            + "or e/KEYWORD [MORE_KEYWORDS]... or r/KEYWORD [MORE_KEYWORDS]... or l/LABEL_NAME\n"
            + "Examples: " + COMMAND_WORD + " n/aiken dueet; " + COMMAND_WORD + " l/Tutorial 1";

    private static final Logger logger = LogsCenter.getLogger(FindCommand.class);

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        logger.info("Find completed: " + model.getFilteredPersonList().size() + " student(s) matched.");
        return new CommandResult(
                Messages.formatStudentsListed(model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
