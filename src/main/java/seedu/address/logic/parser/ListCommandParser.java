package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
// @@author ananyakharbanda

/**
 * Parses input arguments and creates a new {@code ListCommand}.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses the given arguments in the context of the {@code ListCommand}.
     *
     * @param args arguments supplied after the command word.
     * @return a command that lists every student.
     * @throws ParseException if any arguments are supplied.
     */
    public ListCommand parse(String args) throws ParseException {
        // @@author
        if (!StringUtil.normaliseSpaces(args).isBlank()) {
            // @@author ananyakharbanda
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }
        return new ListCommand();
    }
}
            // @@author

