package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;

public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_noArguments_returnsListCommand() throws Exception {
        // EP: empty and whitespace-only argument strings are accepted.
        // BVA: zero non-whitespace characters.
        assertTrue(parser.parse("") instanceof ListCommand);
        assertTrue(parser.parse("   \t") instanceof ListCommand);
    }

    @Test
    public void parse_argumentsPresent_throwsParseException() {
        // EP: positional and parameter-like arguments are rejected.
        // BVA: one non-whitespace character is already too many.
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 1", expectedMessage);
        assertParseFailure(parser, " n/Alex", expectedMessage);
    }
}
