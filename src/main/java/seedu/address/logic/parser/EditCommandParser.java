package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DELETE_LABEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LABEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.label.Label;
import seedu.address.model.person.StudentId;

/**
 * Parses input arguments and creates a new EditCommand object.
 */
public class EditCommandParser implements Parser<EditCommand> {

    private static final List<Prefix> SUPPORTED_PREFIXES =
            List.of(PREFIX_STUDENT_ID, PREFIX_NAME, PREFIX_EMAIL, PREFIX_REMARK,
                    PREFIX_LABEL, PREFIX_DELETE_LABEL);
    private static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %s.";
    private static final String MESSAGE_MISSING_STUDENT_ID = "Missing required parameter: i/.";
    private static final String MESSAGE_EMPTY_STUDENT_ID = "Parameter i/ cannot be empty.";
    private static final String MESSAGE_EMPTY_DELETE_LABEL = "Parameter dl/ cannot be empty.";
    private static final String MESSAGE_CONFLICTING_LABEL_CHANGE =
            "An empty l/ cannot be combined with other label changes.";
    private static final String MESSAGE_DUPLICATE_LABEL = "Parameter %1$s repeats label \"%2$s\".";

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = StringUtil.trimSpaces(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(trimmedArgs,
                PREFIX_STUDENT_ID, PREFIX_NAME, PREFIX_EMAIL, PREFIX_REMARK, PREFIX_LABEL, PREFIX_DELETE_LABEL);
        validateStructure(trimmedArgs, arguments);
        StudentId studentId = parseStudentId(arguments);
        EditPersonDescriptor editPersonDescriptor = parseEditFields(arguments);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(studentId, editPersonDescriptor);
    }

    private static void validateStructure(String args, ArgumentMultimap arguments) throws ParseException {
        if (args.contains("\n") || args.contains("\r")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }
        rejectUnknownPrefixes(args);
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT_ID, PREFIX_NAME, PREFIX_EMAIL, PREFIX_REMARK);
    }

    private static EditPersonDescriptor parseEditFields(ArgumentMultimap arguments) throws ParseException {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        if (arguments.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(ParserUtil.parseName(arguments.getValue(PREFIX_NAME).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_EMAIL).isPresent()) {
            descriptor.setEmail(ParserUtil.parseEmail(arguments.getValue(PREFIX_EMAIL).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_REMARK).isPresent()) {
            descriptor.setRemark(ParserUtil.parseRemark(arguments.getValue(PREFIX_REMARK).orElseThrow()));
        }
        parseLabelChanges(arguments, descriptor);
        return descriptor;
    }

    private static void parseLabelChanges(ArgumentMultimap arguments, EditPersonDescriptor descriptor)
            throws ParseException {
        List<String> additions = arguments.getAllValues(PREFIX_LABEL);
        List<String> removals = arguments.getAllValues(PREFIX_DELETE_LABEL);
        if (additions.contains("")) {
            if (additions.size() != 1 || !removals.isEmpty()) {
                throw new ParseException(MESSAGE_CONFLICTING_LABEL_CHANGE);
            }
            descriptor.setClearingLabels(true);
            return;
        }
        descriptor.setLabelsToAdd(parseLabels(additions, PREFIX_LABEL));
        descriptor.setLabelsToRemove(parseLabels(removals, PREFIX_DELETE_LABEL));
    }

    private static List<Label> parseLabels(List<String> values, Prefix prefix) throws ParseException {
        List<Label> labels = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (String value : values) {
            if (value.isEmpty()) {
                throw new ParseException(MESSAGE_EMPTY_DELETE_LABEL);
            }
            Label label = ParserUtil.parseLabel(value);
            if (!names.add(label.labelName.toLowerCase(Locale.ROOT))) {
                throw new ParseException(String.format(MESSAGE_DUPLICATE_LABEL, prefix, label.labelName));
            }
            labels.add(label);
        }
        return labels;
    }

    private static StudentId parseStudentId(ArgumentMultimap arguments) throws ParseException {
        Optional<String> studentId = arguments.getValue(PREFIX_STUDENT_ID);
        if (studentId.isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_STUDENT_ID);
        }
        if (studentId.get().isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_STUDENT_ID);
        }
        return ParserUtil.parseStudentId(studentId.get());
    }

    private static void rejectUnknownPrefixes(String args) throws ParseException {
        Optional<Prefix> unknownPrefix = ArgumentTokenizer.findUnknownPrefix(args, SUPPORTED_PREFIXES);
        if (unknownPrefix.isPresent()) {
            throw new ParseException(String.format(MESSAGE_UNKNOWN_PARAMETER, unknownPrefix.get()));
        }
    }

}
