package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_DATE;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.label.Label;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Remark;
import seedu.address.model.person.StudentId;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_STUDENT_ID = "A012345-B";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_LABEL = "Tutorial/1";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_STUDENT_ID = "A0123456B";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_REMARK = "Quiz 1: 8/10. Consultation on Monday at 2pm.";
    private static final String VALID_LABEL = "Discrete Math Tutorial";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parse_unicodeSpaces_normalisesBeforeValidation() throws Exception {
        // EP: Unicode padding is trimmed; labels and remarks preserve internal characters.
        for (String space : new String[] {"\u00a0", "\u2007", "\u2009", "\u202f", "\u3000",
            "\u115f", "\u1160", "\u3164", "\uffa0"}) {
            assertEquals(new Name("Rachel Walker"), ParserUtil.parseName(space + "Rachel" + space + "Walker" + space));
            assertEquals(new StudentId(VALID_STUDENT_ID), ParserUtil.parseStudentId(space + VALID_STUDENT_ID + space));
            assertEquals(new Email(VALID_EMAIL), ParserUtil.parseEmail(space + VALID_EMAIL + space));
            assertEquals(new Label("Tutorial" + space + space + "1"),
                    ParserUtil.parseLabel(space + "Tutorial" + space + space + "1"));
            assertEquals(new Remark("Follow" + space + "up\nNext week"),
                    ParserUtil.parseRemark(space + "Follow" + space + "up\nNext week" + space));
            assertEquals(LocalDate.parse("2026-10-20"), ParserUtil.parseDate(space + "2026-10-20" + space));
            assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex(space + "1" + space));
            assertThrows(ParseException.class, () -> ParserUtil.parseStudentId("A012" + space + "3456B"));
            assertThrows(ParseException.class, () -> ParserUtil.parseEmail("rachel" + space + "@example.com"));
            assertThrows(ParseException.class, () -> ParserUtil.parseDate("2026-10-" + space + "20"));
            // BVA: Zero characters after trimming is valid only for the optional remark.
            assertEquals(new Remark(""), ParserUtil.parseRemark(space));
            assertThrows(ParseException.class, () -> ParserUtil.parseName(space));
            assertThrows(ParseException.class, () -> ParserUtil.parseLabel(space));
            assertThrows(ParseException.class, () -> ParserUtil.parseStudentId(space));
            assertThrows(ParseException.class, () -> ParserUtil.parseEmail(space));
            assertThrows(ParseException.class, () -> ParserUtil.parseDate(space));
            assertThrows(ParseException.class, () -> ParserUtil.parseIndex(space));
        }
    }

    @Test
    public void parseLabel_unicodeSpaces_checksNormalisedLengthBoundaries() throws Exception {
        // BVA: A label at the maximum length is accepted; one character above it is rejected after normalisation.
        String label = "a".repeat(Label.MAX_NAME_LENGTH - 2) + "\u00a0b";
        assertEquals(new Label(label),
                ParserUtil.parseLabel("\u3000" + label + "\u202f"));
        assertThrows(ParseException.class, () -> ParserUtil.parseLabel("\u3000" + label + "b\u202f"));
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        // EP: invalid input
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        // EP: out of range input
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // EP: valid input
        // EP: valid index without surrounding whitespace
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // EP: valid index with surrounding whitespace
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        // EP: null
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        // EP: invalid value
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        // EP: valid value without whitespace
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        // EP: valid value with whitespace
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_unicodeAndRepeatedSpaces_returnsNormalisedName() throws Exception {
        // EP: unicode and repeated spaces
        assertEquals("José O’Neill", ParserUtil.parseName("  José   O’Neill  ").fullName);
        String name = "A".repeat(Name.MAX_LENGTH - 2) + "   B";
        assertEquals("A".repeat(Name.MAX_LENGTH - 2) + " B", ParserUtil.parseName(name).fullName);
    }

    @Test
    public void parseName_digitsOrOversizedName_throwsParseException() {
        // EP: name contains a digit
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseName("John 2"));

        // EP: name exceeds the maximum length
        // BVA: one character over the limit
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseName("A".repeat(Name.MAX_LENGTH + 1)));
    }

    @Test
    public void parseStudentId_null_throwsNullPointerException() {
        // EP: null
        assertThrows(NullPointerException.class, () -> ParserUtil.parseStudentId((String) null));
    }

    @Test
    public void parseStudentId_invalidValue_throwsParseException() {
        // EP: invalid value
        assertThrows(ParseException.class, () -> ParserUtil.parseStudentId(INVALID_STUDENT_ID));
    }

    @Test
    public void parseStudentId_validValueWithoutWhitespace_returnsStudentId() throws Exception {
        // EP: valid value without whitespace
        StudentId expectedStudentId = new StudentId(VALID_STUDENT_ID);
        assertEquals(expectedStudentId, ParserUtil.parseStudentId(VALID_STUDENT_ID));
    }

    @Test
    public void parseStudentId_validValueWithWhitespace_returnsTrimmedStudentId() throws Exception {
        // EP: valid value with whitespace
        String studentIdWithWhitespace = WHITESPACE + VALID_STUDENT_ID.toLowerCase() + WHITESPACE;
        StudentId expectedStudentId = new StudentId(VALID_STUDENT_ID);
        assertEquals(expectedStudentId, ParserUtil.parseStudentId(studentIdWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        // EP: null
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        // EP: invalid value
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        // EP: valid value without whitespace
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        // EP: valid value with whitespace
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseEmail_uppercaseDomain_returnsNormalisedEmail() throws Exception {
        // EP: uppercase domain
        assertEquals("Rachel+Quiz@example.com", ParserUtil.parseEmail("  Rachel+Quiz@EXAMPLE.COM  ").value);
    }

    @Test
    public void parseEmail_invalidDomainOrOversizedLocalPart_throwsParseException() {
        // EP: email lacks a dotted domain
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseEmail("a@localhost"));

        // EP: local part exceeds the maximum length
        // BVA: 65 characters in the local part
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseEmail("a".repeat(65) + "@example.com"));
    }

    @Test
    public void parseRemark_null_throwsNullPointerException() {
        // EP: null
        assertThrows(NullPointerException.class, () -> ParserUtil.parseRemark(null));
    }

    @Test
    public void parseRemark_empty_returnsEmptyRemark() throws Exception {
        // EP: empty
        // BVA: zero input characters
        assertEquals(new Remark(""), ParserUtil.parseRemark(""));
    }

    @Test
    public void parseRemark_whitespaceOnly_returnsEmptyRemark() throws Exception {
        // EP: whitespace only
        // BVA: zero characters after trimming
        assertEquals(new Remark(""), ParserUtil.parseRemark(WHITESPACE));
    }

    @Test
    public void parseRemark_validValueWithoutWhitespace_returnsRemark() throws Exception {
        // EP: valid value without whitespace
        assertEquals(new Remark(VALID_REMARK), ParserUtil.parseRemark(VALID_REMARK));
    }

    @Test
    public void parseRemark_validValueWithWhitespace_returnsTrimmedRemark() throws Exception {
        // EP: valid value with whitespace
        assertEquals(new Remark(VALID_REMARK), ParserUtil.parseRemark(WHITESPACE + VALID_REMARK + WHITESPACE));
    }

    @Test
    public void parseRemark_internalWhitespaceAndCase_preservesText() throws Exception {
        // EP: internal whitespace and case
        String remark = "Needs  HELP\twith recursion. Quiz: 8/10!";
        assertEquals(new Remark(remark), ParserUtil.parseRemark(WHITESPACE + remark + WHITESPACE));
    }

    @Test
    public void parseRemark_maximumLengthAfterTrimming_returnsRemark() throws Exception {
        // EP: valid remark at the maximum length after trimming
        // BVA: 4000 code points
        String remark = "x".repeat(Remark.MAX_LENGTH);
        assertEquals(new Remark(remark), ParserUtil.parseRemark(WHITESPACE + remark + WHITESPACE));
    }

    @Test
    public void parseRemark_overMaximumLengthAfterTrimming_throwsParseException() {
        // EP: remark exceeds the maximum length after trimming
        // BVA: 4001 code points
        String remark = "x".repeat(Remark.MAX_LENGTH + 1);
        assertThrows(ParseException.class, Remark.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseRemark(WHITESPACE + remark + WHITESPACE));
    }

    @Test
    public void parseRemark_supplementaryUnicode_countsCodePoints() throws Exception {
        // EP: supplementary Unicode at the code-point limit
        String remark = "\uD83D\uDE00".repeat(Remark.MAX_LENGTH);
        assertEquals(new Remark(remark), ParserUtil.parseRemark(WHITESPACE + remark + WHITESPACE));
        // EP: supplementary Unicode over the code-point limit
        // BVA: one code point over the maximum
        assertThrows(ParseException.class, Remark.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRemark(remark + "x"));
    }

    @Test
    public void parseLabel_null_throwsNullPointerException() {
        // EP: null
        assertThrows(NullPointerException.class, () -> ParserUtil.parseLabel(null));
    }

    @Test
    public void parseLabel_invalidValue_throwsParseException() {
        // EP: invalid value
        assertThrows(ParseException.class, () -> ParserUtil.parseLabel(INVALID_LABEL));

        // EP: Unicode lowercasing expands a label beyond the maximum length
        // BVA: 31 dotted capital I characters become 62 code points
        assertThrows(ParseException.class, Label.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseLabel("İ".repeat(31)));
    }

    @Test
    public void parseLabel_validValueWithoutWhitespace_returnsLabel() throws Exception {
        // EP: valid value without whitespace
        Label expectedLabel = new Label(VALID_LABEL);
        assertEquals(expectedLabel, ParserUtil.parseLabel(VALID_LABEL));

        // EP: mixed-case label is stored in lowercase
        assertEquals("tut1", ParserUtil.parseLabel("TuT1").labelName);

        // EP: Unicode lowercasing expands to a label at the maximum length
        // BVA: 30 dotted capital I characters become 60 code points
        assertEquals(new Label("İ".repeat(30)), ParserUtil.parseLabel("İ".repeat(30)));
    }

    @Test
    public void parseLabel_validValueWithWhitespace_returnsTrimmedLabel() throws Exception {
        // EP: valid value with whitespace
        String labelWithWhitespace = WHITESPACE + VALID_LABEL + WHITESPACE;
        Label expectedLabel = new Label(VALID_LABEL);
        assertEquals(expectedLabel, ParserUtil.parseLabel(labelWithWhitespace));
    }

    @Test
    public void parseDate_null_throwsNullPointerException() {
        // EP: null input
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDate(null));
    }

    @Test
    public void parseDate_invalidFormat_throwsParseException() {
        // EP: invalid format regex
        assertThrows(ParseException.class, MESSAGE_INVALID_DATE, () -> ParserUtil.parseDate("2024/05/10"));
        assertThrows(ParseException.class, MESSAGE_INVALID_DATE, () -> ParserUtil.parseDate("2024-5-1"));
        assertThrows(ParseException.class, MESSAGE_INVALID_DATE, () -> ParserUtil.parseDate("not-a-date"));
    }

    @Test
    public void parseDate_invalidDate_throwsParseException() {
        // EP: nonexistent calendar date matching regex
        // BVA: day exceeds February limit
        assertThrows(ParseException.class, MESSAGE_INVALID_DATE, () -> ParserUtil.parseDate("2024-02-30"));
        // BVA: month exceeds 12-month limit
        assertThrows(ParseException.class, MESSAGE_INVALID_DATE, () -> ParserUtil.parseDate("2024-13-01"));
    }

    @Test
    public void parseDate_validDateWithoutWhitespace_returnsDate() throws Exception {
        // EP: valid date without whitespace
        assertEquals(LocalDate.parse("2024-05-10"), ParserUtil.parseDate("2024-05-10"));
    }

    @Test
    public void parseDate_validDateWithWhitespace_returnsDate() throws Exception {
        // EP: valid date with whitespace
        assertEquals(LocalDate.parse("2024-05-10"), ParserUtil.parseDate(WHITESPACE + "2024-05-10" + WHITESPACE));
    }

}
