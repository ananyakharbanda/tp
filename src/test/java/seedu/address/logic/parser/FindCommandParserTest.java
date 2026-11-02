package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.label.Label;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.LabelMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.RemarkContainsKeywordsPredicate;
import seedu.address.model.person.StudentIdContainsKeywordsPredicate;

public class FindCommandParserTest {
    private static final String SEARCH_PARAMETER = "n/, i/, e/, r/, or l/";
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_missingSearchField_reportsMissingParameter() {
        // EP: no field, whether input is empty, blank, or unprefixed text.
        // BVA: zero search fields (one required).
        for (String input : List.of("", " \t ", "Alice Bob")) {
            assertParseFailure(parser, input, "Missing required parameter: " + SEARCH_PARAMETER + ".");
        }
    }

    @Test
    public void parse_eachKeywordField_returnsExpectedPredicate() {
        // EP: each keyword prefix; whitespace separates keywords and is trimmed.
        // BVA: one keyword and two keywords; one-character keyword is the minimum nonempty value.
        for (List<String> keywords : List.of(List.of("A"), List.of("Alice", "Bob"))) {
            String value = String.join(" \t\n ", keywords);
            assertParseSuccess(parser, " n/ " + value + " ",
                    new FindCommand(new NameContainsKeywordsPredicate(keywords)));
            assertParseSuccess(parser, "i/" + value,
                    new FindCommand(new StudentIdContainsKeywordsPredicate(keywords)));
            assertParseSuccess(parser, "e/" + value,
                    new FindCommand(new EmailContainsKeywordsPredicate(keywords)));
            assertParseSuccess(parser, "r/" + value,
                    new FindCommand(new RemarkContainsKeywordsPredicate(keywords)));
        }
    }

    @Test
    public void parse_emptyOrRepeatedField_reportsSpecificParameter() {
        // EP: empty value and repeated same field are rejected for each supported prefix.
        // BVA: value length zero; two occurrences, immediately above the allowed one.
        for (String prefix : List.of("n/", "i/", "e/", "r/", "l/")) {
            for (String blank : List.of("", " \n\t ")) {
                assertParseFailure(parser, prefix + blank, "Parameter " + prefix + " cannot be empty.");
            }
            assertParseFailure(parser, prefix + "One " + prefix + "Two",
                    "Parameter " + prefix + " must be specified only once.");
        }
    }

    @Test
    public void parse_multipleDifferentFields_reportsMultipleFields() {
        // EP: distinct supported search fields cannot be combined, regardless of order.
        // BVA: two distinct fields, immediately above the allowed one.
        List<String> prefixes = List.of("n/", "i/", "e/", "r/", "l/");
        for (int index = 0; index < prefixes.size(); index++) {
            String first = prefixes.get(index);
            String second = prefixes.get((index + 1) % prefixes.size());
            assertParseFailure(parser, first + "One " + second + "Two",
                    "Parameter " + SEARCH_PARAMETER + " must be specified only once.");
        }
    }

    @Test
    public void parse_structureErrors_respectErrorPrecedence() {
        // EP: preamble errors precede unknown fields; unknown fields precede repeated fields.
        String invalidFormat = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String input : List.of("extra n/One", "extra x/One", "extra n/One n/Two")) {
            assertParseFailure(parser, input, invalidFormat);
        }
        assertParseFailure(parser, "x/One", "Unknown parameter: x/.");
        assertParseFailure(parser, "n/One n/Two x/Three", "Unknown parameter: x/.");
        // EP: prefixes are case-sensitive, including every otherwise supported field.
        for (String prefix : List.of("N/", "I/", "E/", "R/", "L/")) {
            assertParseFailure(parser, prefix + "One", "Unknown parameter: " + prefix + ".");
        }
        // EP: repeated-field errors precede multiple-field and empty-value errors.
        assertParseFailure(parser, "l/ l/Two n/One", "Parameter l/ must be specified only once.");
        assertParseFailure(parser, "n/ l/One", "Parameter " + SEARCH_PARAMETER + " must be specified only once.");
    }

    @Test
    public void parse_literalSlashText_isKeywordContent() {
        // EP: numeric slash text and URLs are values, not unknown alphabetic prefixes.
        assertParseSuccess(parser, "r/8/10 https://example.com/a/b",
                new FindCommand(new RemarkContainsKeywordsPredicate(List.of("8/10", "https://example.com/a/b"))));
    }

    @Test
    public void parse_validLabel_preservesCompleteNameAndWhitespace() {
        // EP: label values remain complete names; internal spaces do not split them into keywords.
        assertParseSuccess(parser, " \tl/ Tutorial  1 \t",
                new FindCommand(new LabelMatchesPredicate(new Label("Tutorial  1"))));
        // EP: digit-only labels, combining marks, and the permitted punctuation remain valid label content.
        for (String label : List.of("1", "e\u0301", "Group_1 (A). O'Neil-2")) {
            assertParseSuccess(parser, "l/" + label, new FindCommand(new LabelMatchesPredicate(new Label(label))));
        }
    }

    @Test
    public void parse_labelLengthBoundaries_countsUnicodeCodePoints() {
        // EP: valid labels include ASCII and supplementary Unicode letters.
        // BVA: 1, 59, 60 code points accepted; 61 rejected. Zero is covered by empty-field tests.
        for (String letter : List.of("A", "\uD801\uDC00")) {
            for (int length : List.of(1, 59, 60)) {
                String label = letter.repeat(length);
                assertParseSuccess(parser, "l/" + label,
                        new FindCommand(new LabelMatchesPredicate(new Label(label))));
            }
            assertParseFailure(parser, "l/" + letter.repeat(61), Label.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidLabel_reportsLabelConstraints() {
        // EP: no letter/digit, disallowed punctuation, slash, and embedded control characters.
        for (String label : List.of("---", "Tutorial!", "Tutorial/1", "Tutorial\n1", "Tutorial\t1", "A\u007fB")) {
            assertParseFailure(parser, "l/" + label, Label.MESSAGE_CONSTRAINTS);
        }
    }
}
