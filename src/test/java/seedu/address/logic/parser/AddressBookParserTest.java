package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AttendanceCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.UnmarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.label.Label;
import seedu.address.model.person.LabelMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentId;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_unicodeSpaces_matchesAsciiCommand() throws Exception {
        // EP: Unicode spaces and Hangul fillers separate the command, prefixes, and search keywords.
        for (String space : List.of("\u00a0", "\u2007", "\u2009", "\u202f", "\u3000",
                "\u115f", "\u1160", "\u3164", "\uffa0")) {
            Person expected = new PersonBuilder().withName("Alice Pauline").withStudentId("A1234567B")
                    .withEmail("alice@example.com").withLabels("Tutorial" + space + "1").withRemark("").build();
            assertEquals(new AddCommand(expected),
                    parser.parseCommand(space + "add" + space + "n/Alice" + space + "Pauline" + space
                            + "i/A1234567B" + space + "e/alice@example.com" + space + "l/Tutorial" + space + "1"));
            assertEquals(parser.parseCommand("find n/Alice Pauline"),
                    parser.parseCommand("find" + space + "n/Alice" + space + "Pauline"));
            assertThrows(ParseException.class, "Unknown parameter: x/.", () ->
                    parser.parseCommand("add n/Alice i/A1234567B e/alice@example.com" + space + "x/unknown"));
            // BVA: Unicode-space-only arguments have zero meaningful characters for commands accepting no arguments.
            assertTrue(parser.parseCommand("list" + space) instanceof ListCommand);
            assertTrue(new UndoCommandParser().parse(space) instanceof UndoCommand);
            assertTrue(new ListCommandParser().parse(space) instanceof ListCommand);
        }
    }

    @Test
    public void parseCommand_addAndEdit_preserveUnicodeRemarkAndLabels() throws Exception {
        for (String space : List.of(" ", "\u00a0", "\u2007", "\u2009", "\u202f", "\u3000",
                "\u115f", "\u1160", "\u3164", "\uffa0")) {
            String remark = "Meet" + space + space + "at 2pm";
            String label = "Tutorial" + space + space + "1";
            String fields = space + "r/" + space + remark + space + "l/" + space + label + space;
            Person person = new PersonBuilder().withName("Alice Pauline").withStudentId("A1234567B")
                    .withEmail("alice@example.com").withRemark(remark).withLabels(label).build();
            assertEquals(new AddCommand(person), parser.parseCommand(space + "add" + space
                    + "n/Alice Pauline i/A1234567B e/alice@example.com" + fields));
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withRemark(remark)
                    .withLabelsToAdd(label).build();
            assertEquals(new EditCommand(person.getStudentId(), descriptor),
                    parser.parseCommand(space + "edit" + space + "i/A1234567B" + fields));
            EditPersonDescriptor removal = new EditPersonDescriptorBuilder().withLabelsToRemove(label).build();
            assertEquals(new EditCommand(person.getStudentId(), removal),
                    parser.parseCommand("edit i/A1234567B" + space + "dl/" + space + label + space));
            assertEquals(new FindCommand(new LabelMatchesPredicate(new Label(label))),
                    parser.parseCommand("find" + space + "l/" + space + label + space));
            LocalDate date = LocalDate.parse("2026-10-20");
            assertEquals(new MarkCommand(person.getStudentId(), date, new Label(label)),
                    parser.parseCommand("mark i/A1234567B d/2026-10-20 l/" + label));
            assertEquals(new UnmarkCommand(person.getStudentId(), date, new Label(label)),
                    parser.parseCommand("unmark i/A1234567B d/2026-10-20 l/" + label));
            assertEquals(new AttendanceCommand(date, new Label(label)),
                    parser.parseCommand("attendance d/2026-10-20 l/" + label));
        }
    }

    @Test
    public void parseCommand_unmark_returnsUnmarkCommand() throws Exception {
        UnmarkCommand expected = new UnmarkCommand(new StudentId("A0123456B"),
                LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        assertEquals(expected, parser.parseCommand("unmark\ti/a0123456b d/2026-10-20 l/Tutorial 1"));
    }

    @Test
    public void parseCommand_unmarkInvalidParameters_reportsSpecificErrors() {
        assertThrows(ParseException.class, "Missing required parameter: i/.", () -> parser.parseCommand("unmark"));
        assertThrows(ParseException.class, UnmarkCommand.MESSAGE_INVALID_FORMAT, () -> parser.parseCommand("unmark 1"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_DATE, () ->
                parser.parseCommand("unmark i/A0123456B d/2026-02-30 l/Tutorial 1"));
    }

    @Test
    public void parseCommand_addWithTabs_success() throws Exception {
        // EP: add command with tab-separated parameters
        Person person = new PersonBuilder().withRemark("Quiz: 8/10.").build();
        String command = "add\tn/" + person.getName() + "\ti/" + person.getStudentId()
                + "\te/" + person.getEmail() + "\tr/" + person.getRemark();
        Person expected = new PersonBuilder(person).build();
        assertEquals(new AddCommand(expected), parser.parseCommand(command));
    }

    @Test
    public void parseCommand_addInvalidParameters_throwsSpecificErrors() {
        // EP: add command with missing or unknown parameters
        assertThrows(ParseException.class, "Missing required parameter: i/.", ()
            -> parser.parseCommand("add n/Samuel e/samuel@example.com"));
        assertThrows(ParseException.class, "Unknown parameter: p/.", ()
            -> parser.parseCommand("add n/Samuel i/A0123456B e/samuel@example.com p/123"));
    }

    @Test
    public void parseCommand_add() throws Exception {
        // EP: valid add command dispatch
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        // EP: no arguments or whitespace-only arguments route to clear.
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " \t") instanceof ClearCommand);
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(ClearCommand.COMMAND_WORD + " 1"));
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(ClearCommand.COMMAND_WORD + " n/Alex"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        // EP: valid delete command dispatch with lowercase Student ID
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                "delete\ti/a0123456b");
        assertEquals(new DeleteCommand(new StudentId("A0123456B")), command);
    }

    @Test
    public void parseCommand_deleteInvalidParameters_reportsSpecificErrors() {
        // EP: delete command with missing or invalid ID
        assertThrows(ParseException.class, "Missing required parameter: i/.", () -> parser.parseCommand("delete"));
        assertThrows(ParseException.class, DeleteCommand.MESSAGE_INVALID_FORMAT, () -> parser.parseCommand("delete 1"));
        assertThrows(ParseException.class, StudentId.MESSAGE_CONSTRAINTS, () -> parser.parseCommand("delete i/123"));
    }

    @Test
    public void parseCommand_edit() throws Exception {
        // EP: valid edit command dispatch
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + "i/" + person.getStudentId() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(person.getStudentId(), descriptor), command);
    }

    @Test
    public void parseCommand_attendance() throws Exception {
        AttendanceCommand command = (AttendanceCommand) parser.parseCommand(
                AttendanceCommand.COMMAND_WORD + " d/2026-10-20 l/Tutorial 1");
        assertEquals(new AttendanceCommand(LocalDate.parse("2026-10-20"), new Label("Tutorial 1")), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        // EP: no arguments or whitespace-only arguments route to exit.
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " \t") instanceof ExitCommand);
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(ExitCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(ExitCommand.COMMAND_WORD + " n/Alex"));
    }

    @Test
    public void parseCommand_findLabel() throws Exception {
        assertEquals(new FindCommand(new LabelMatchesPredicate(new Label("Discrete Math Tutorial"))),
                parser.parseCommand("find l/Discrete Math Tutorial"));
    }

    @Test
    public void parseCommand_removedFilter_throwsUnknownCommand() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("filter"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, ()
            -> parser.parseCommand("filter l/Discrete Math Tutorial"));
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " n/" + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        // EP: no arguments or whitespace-only arguments route to help.
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " \t") instanceof HelpCommand);
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(HelpCommand.COMMAND_WORD + " 3"));
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand(HelpCommand.COMMAND_WORD + " n/Alex"));
    }

    @Test
    public void parseCommand_list() throws Exception {
        // EP: list is routed through its argument-validating parser.
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        String invalidCommand = ListCommand.COMMAND_WORD + " 3";
        Executable parseInvalidCommand = () -> parser.parseCommand(invalidCommand);
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE),
                parseInvalidCommand);
    }

    @Test
    public void parseCommand_undo_returnsUndoCommand() throws Exception {
        // EP: undo accepts surrounding whitespace and is routed to its parser.
        assertEquals(new UndoCommand(), parser.parseCommand("undo"));
        assertEquals(new UndoCommand(), parser.parseCommand("  undo  "));
    }

    @Test
    public void parseCommand_removedLabelCommand_throwsUnknownCommand() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("label"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, ()
            -> parser.parseCommand("label l/Discrete Math Tutorial i/A0101010A"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        // EP: unsupported command words and incorrectly capitalized words are rejected.
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("List"));
    }
}
