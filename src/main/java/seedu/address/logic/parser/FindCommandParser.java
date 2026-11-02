package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LABEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.LabelMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.RemarkContainsKeywordsPredicate;
import seedu.address.model.person.StudentIdContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?:^|\\p{javaWhitespace})([A-Za-z]+/)");
    private static final Set<Prefix> SUPPORTED_PREFIXES =
            Set.of(PREFIX_NAME, PREFIX_STUDENT_ID, PREFIX_EMAIL, PREFIX_REMARK, PREFIX_LABEL);
    private static final String SEARCH_PARAMETER =
            PREFIX_NAME + ", " + PREFIX_STUDENT_ID + ", " + PREFIX_EMAIL
                    + ", " + PREFIX_REMARK + ", or " + PREFIX_LABEL;
    private static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %s.";
    private static final String MESSAGE_REPEATED_PARAMETER = "Parameter %s must be specified only once.";
    private static final String MESSAGE_MISSING_PARAMETER = "Missing required parameter: %s.";
    private static final String MESSAGE_EMPTY_PARAMETER = "Parameter %s cannot be empty.";

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = StringUtil.trimSpaces(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(trimmedArgs, PREFIX_NAME, PREFIX_STUDENT_ID,
                        PREFIX_EMAIL, PREFIX_REMARK, PREFIX_LABEL);
        validateStructure(trimmedArgs, argMultimap);

        Predicate<Person> predicate;
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            predicate = new NameContainsKeywordsPredicate(parseKeywords(argMultimap, PREFIX_NAME));
        } else if (argMultimap.getValue(PREFIX_STUDENT_ID).isPresent()) {
            predicate = new StudentIdContainsKeywordsPredicate(parseKeywords(argMultimap, PREFIX_STUDENT_ID));
        } else if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            predicate = new EmailContainsKeywordsPredicate(parseKeywords(argMultimap, PREFIX_EMAIL));
        } else if (argMultimap.getValue(PREFIX_REMARK).isPresent()) {
            predicate = new RemarkContainsKeywordsPredicate(parseKeywords(argMultimap, PREFIX_REMARK));
        } else {
            predicate = new LabelMatchesPredicate(ParserUtil.parseLabel(parseSearchValue(argMultimap, PREFIX_LABEL)));
        }

        return new FindCommand(predicate);
    }

    private static void validateStructure(String args, ArgumentMultimap arguments) throws ParseException {
        rejectWordsBeforeFirstPrefix(args);
        rejectUnknownPrefixes(args);
        rejectRepeatedPrefixes(arguments);
        long searchPrefixCount = SUPPORTED_PREFIXES.stream()
                .filter(prefix -> arguments.getValue(prefix).isPresent())
                .count();
        if (searchPrefixCount == 0) {
            throw new ParseException(String.format(MESSAGE_MISSING_PARAMETER, SEARCH_PARAMETER));
        }
        if (searchPrefixCount > 1) {
            throw new ParseException(String.format(MESSAGE_REPEATED_PARAMETER, SEARCH_PARAMETER));
        }
    }

    private static void rejectWordsBeforeFirstPrefix(String args) throws ParseException {
        Matcher matcher = PARAMETER_PREFIX.matcher(StringUtil.normaliseSpaces(args));
        if (matcher.find() && !StringUtil.trimSpaces(args.substring(0, matcher.start(1))).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
    }

    private static void rejectUnknownPrefixes(String args) throws ParseException {
        Optional<Prefix> unknownPrefix = ArgumentTokenizer.findUnknownPrefix(args, SUPPORTED_PREFIXES);
        if (unknownPrefix.isPresent()) {
            throw new ParseException(String.format(MESSAGE_UNKNOWN_PARAMETER, unknownPrefix.get()));
        }
    }

    private static void rejectRepeatedPrefixes(ArgumentMultimap arguments) throws ParseException {
        for (Prefix prefix : SUPPORTED_PREFIXES) {
            if (arguments.getAllValues(prefix).size() > 1) {
                throw new ParseException(String.format(MESSAGE_REPEATED_PARAMETER, prefix));
            }
        }
    }

    /**
     * Parses the keywords belonging to {@code prefix}.
     */
    private static List<String> parseKeywords(ArgumentMultimap argumentMultimap, Prefix prefix) throws ParseException {
        return List.of(StringUtil.normaliseSpaces(parseSearchValue(argumentMultimap, prefix)).split("\\s+"));
    }

    private static String parseSearchValue(ArgumentMultimap argumentMultimap, Prefix prefix) throws ParseException {
        String value = argumentMultimap.getValue(prefix).orElseThrow();
        if (value.isBlank()) {
            throw new ParseException(String.format(MESSAGE_EMPTY_PARAMETER, prefix));
        }
        return value;
    }
}
