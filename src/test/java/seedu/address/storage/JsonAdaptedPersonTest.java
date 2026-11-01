package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MESSAGE_DUPLICATE_ATTENDANCE;
import static seedu.address.storage.JsonAdaptedPerson.MESSAGE_DUPLICATE_LABEL;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_ATTENDANCE_MESSAGE;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_LABEL_MESSAGE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.attendance.AttendanceRecord;
import seedu.address.model.label.Label;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.model.person.StudentId;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_STUDENT_ID = "A012345-B";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_LABEL = "Tutorial/1";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_STUDENT_ID = BENSON.getStudentId().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final List<JsonAdaptedLabel> VALID_LABELS = List.of(
            new JsonAdaptedLabel("Discrete Math Tutorial"));

    @Test
    public void toModelType_legacyMixedCaseGroups_storesLowercase() throws Exception {
        // EP: older JSON with mixed-case labels and attendance groups is normalized when loaded
        JsonAdaptedPerson legacyPerson = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(new JsonAdaptedLabel("TuT1")), "",
                List.of(new JsonAdaptedAttendanceRecord("2026-10-20", "TUT1")));

        Person loaded = legacyPerson.toModelType();
        assertEquals(Set.of(new Label("tut1")), loaded.getLabels());
        assertEquals(Set.of(new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("tut1"))),
                loaded.getAttendance());

        String savedJson = JsonUtil.toJsonString(new JsonAdaptedPerson(loaded));
        assertTrue(savedJson.contains("\"tut1\""));
        assertFalse(savedJson.contains("TuT1"));
        assertFalse(savedJson.contains("TUT1"));
    }

    @Test
    public void toModelType_paddedFields_returnsTrimmedPerson() throws Exception {
        // EP: ASCII, Unicode, control whitespace, and Hangul filler padding should be removed when loading.
        // EP: Internal spacing and line breaks remain valid and should be preserved by trimming.
        for (String padding : new String[] {" ", "\t\r\n", "\u00a0", "\u2009", "\u202f", "\u3000",
            "\u115f", "\u1160", "\u3164", "\uffa0"}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(padding + VALID_NAME + padding,
                    padding + "a1234567b" + padding, padding + "Quiz+Sam@EXAMPLE.COM" + padding,
                    List.of(new JsonAdaptedLabel(padding + "Tutorial  1" + padding)),
                    padding + "Follow up  soon\nNext week" + padding,
                    List.of(new JsonAdaptedAttendanceRecord(padding + "2026-10-20" + padding,
                            padding + "Tutorial  1" + padding)));
            Person expected = new PersonBuilder(BENSON).withStudentId("A1234567B")
                    .withEmail("Quiz+Sam@example.com").withLabels("Tutorial  1")
                    .withRemark("Follow up  soon\nNext week")
                    .withAttendance(new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial  1")))
                    .build();

            assertEquals(expected, person.toModelType());
        }
    }

    @Test
    public void toModelType_whitespaceOnlyRequiredFields_throwsIllegalValueException() {
        // EP: Whitespace-only required values are invalid even though the original strings are non-empty.
        // BVA: Trimming leaves zero characters, below the minimum required for each field.
        String whitespace = " \t\r\n\u00a0\u2009\u202f\u3000 ";
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(whitespace, VALID_STUDENT_ID, VALID_EMAIL,
                        List.of(), "").toModelType());
        assertThrows(IllegalValueException.class, StudentId.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(VALID_NAME, whitespace, VALID_EMAIL,
                        List.of(), "").toModelType());
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, whitespace,
                        List.of(), "").toModelType());
        assertThrows(IllegalValueException.class, Label.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedLabel(whitespace).toModelType());
        assertThrows(IllegalValueException.class, JsonAdaptedAttendanceRecord.MESSAGE_INVALID_DATE, () ->
                new JsonAdaptedAttendanceRecord(whitespace, "Tutorial 1").toModelType());
        assertThrows(IllegalValueException.class, Label.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedAttendanceRecord("2026-10-20", whitespace).toModelType());
    }

    @Test
    public void toModelType_whitespaceOnlyRemark_returnsEmptyRemark() throws Exception {
        // EP: Whitespace-only remarks belong to the valid empty optional remark partition after trimming.
        // BVA: Zero characters is the accepted lower length boundary for remarks.
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(), " \t\r\n\u00a0\u2009\u202f\u3000 ");
        assertEquals("", person.toModelType().getRemark().value);
    }

    @Test
    public void toModelType_paddedMaximumLengthFields_validatesAfterTrimming() throws Exception {
        // BVA: Values exactly at each maximum length remain valid when padding is removed before length checks.
        String name = "a".repeat(Name.MAX_LENGTH);
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61);
        String label = "a".repeat(Label.MAX_NAME_LENGTH);
        String remark = "x".repeat(Remark.MAX_LENGTH);
        assertEquals(Email.MAX_LENGTH, email.length());
        JsonAdaptedPerson person = new JsonAdaptedPerson("\u00a0" + name + "\u3000", VALID_STUDENT_ID,
                "\u00a0" + email + "\u3000", List.of(new JsonAdaptedLabel("\u00a0" + label + "\u3000")),
                "\u00a0" + remark + "\u3000",
                List.of(new JsonAdaptedAttendanceRecord("\u00a02026-10-20\u3000", "\u00a0" + label + "\u3000")));
        Person expected = new PersonBuilder(BENSON).withName(name).withEmail(email).withLabels(label)
                .withRemark(remark)
                .withAttendance(new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label(label))).build();

        assertEquals(expected, person.toModelType());
    }

    @Test
    public void toModelType_paddedInvalidFields_throwsIllegalValueException() {
        // EP: Padding cannot make student IDs or emails with internal spaces valid.
        assertThrows(IllegalValueException.class, StudentId.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(VALID_NAME, " A123 4567B ", VALID_EMAIL,
                        List.of(), "").toModelType());
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, " user @example.com ",
                        List.of(), "").toModelType());
        // BVA: Trimmed remarks and labels one character above their maximum lengths must still be rejected.
        assertThrows(IllegalValueException.class, Remark.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                        List.of(), " " + "x".repeat(Remark.MAX_LENGTH + 1) + " ").toModelType());
        assertThrows(IllegalValueException.class, Label.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedLabel(" " + "a".repeat(Label.MAX_NAME_LENGTH + 1) + " ").toModelType());
        // EP: A padded date with the correct format but an impossible calendar day remains invalid.
        assertThrows(IllegalValueException.class, JsonAdaptedAttendanceRecord.MESSAGE_INVALID_DATE, () ->
                new JsonAdaptedAttendanceRecord(" 2026-02-30 ", "Tutorial 1").toModelType());
    }

    @Test
    public void toModelType_internalUnicodeSpacesInRestrictedFields_throwsIllegalValueException() {
        // EP: Internal Unicode spaces are ordinary spaces, which IDs, emails, and dates do not allow.
        assertThrows(IllegalValueException.class, StudentId.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(VALID_NAME, "A123\u00a04567B", VALID_EMAIL, List.of(), "").toModelType());
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, "user\u202f@example.com",
                        List.of(), "").toModelType());
        assertThrows(IllegalValueException.class, JsonAdaptedAttendanceRecord.MESSAGE_INVALID_DATE, () ->
                new JsonAdaptedAttendanceRecord("2026-10-\u300020", "Tutorial 1").toModelType());
    }

    @Test
    public void toModelType_hangulFillerLabels_detectsDuplicatesAndBlankValues() {
        // EP: Filler-only labels are blank; duplicate labels preserve identical internal fillers.
        for (String filler : new String[] {"\u115f", "\u1160", "\u3164", "\uffa0"}) {
            // BVA: A filler-only label has zero meaningful characters after trimming.
            assertThrows(IllegalValueException.class, Label.MESSAGE_CONSTRAINTS, () ->
                    new JsonAdaptedLabel(filler).toModelType());
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                    List.of(new JsonAdaptedLabel("Tutorial" + filler + "1"),
                            new JsonAdaptedLabel("tutorial" + filler + "1")), "");
            assertThrows(IllegalValueException.class, MESSAGE_DUPLICATE_LABEL, person::toModelType);
        }
    }

    @Test
    public void toModelType_paddedDuplicateLabels_throwsIllegalValueException() {
        // EP: Labels with identical internal Unicode spaces differing only in padding and case are duplicates.
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(new JsonAdaptedLabel("Tutorial\u00a01"), new JsonAdaptedLabel(" \ttutorial\u00a01\n")), "");
        assertThrows(IllegalValueException.class, MESSAGE_DUPLICATE_LABEL, person::toModelType);
    }

    @Test
    public void toModelType_paddedDuplicateAttendance_throwsIllegalValueException() {
        // EP: Matching dates and groups after trimming padding and ignoring case are duplicates.
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(), "", List.of(new JsonAdaptedAttendanceRecord("2026-10-20", "Tutorial\u202f1"),
                        new JsonAdaptedAttendanceRecord("\u30002026-10-20\n", " \ttutorial\u202f1\n")));
        assertThrows(IllegalValueException.class, MESSAGE_DUPLICATE_ATTENDANCE, person::toModelType);
    }

    @Test
    public void toModelType_nameAndEmailNormalisation_returnsNormalisedPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson("  Anne-Marie   O’Neill  ", VALID_STUDENT_ID,
                "Quiz+Sam@EXAMPLE.COM", List.of(), "");
        Person restored = person.toModelType();
        assertEquals("Anne-Marie O’Neill", restored.getName().fullName);
        assertEquals("Quiz+Sam@example.com", restored.getEmail().value);
        String savedJson = JsonUtil.toJsonString(new JsonAdaptedPerson(restored));
        assertEquals(restored, JsonUtil.fromJsonString(savedJson, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_digitNameOrSingleLabelDomain_throwsIllegalValueException() {
        JsonAdaptedPerson invalidName = new JsonAdaptedPerson("John 2", VALID_STUDENT_ID,
                VALID_EMAIL, List.of(), "");
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, invalidName::toModelType);
        JsonAdaptedPerson invalidEmail = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID,
                "a@localhost", List.of(), "");
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, invalidEmail::toModelType);
    }

    @Test
    public void toModelType_oversizedRemark_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID,
                VALID_EMAIL, List.of(), "x".repeat(Remark.MAX_LENGTH + 1));
        assertThrows(IllegalValueException.class, Remark.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void jsonAdaptedPerson_maximumLengthRemark_roundTripPreservesText() throws Exception {
        Person original = new PersonBuilder(BENSON).withRemark("x".repeat(Remark.MAX_LENGTH)).build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(original, restored.toModelType());
    }

    @Test
    public void jsonAdaptedPerson_remark_roundTripPreservesText() throws Exception {
        for (String remark : new String[] {"", "Needs help with recursion", "Follow up: 你好！\nNext week"}) {
            Person original = new PersonBuilder(BENSON).withRemark(remark).build();
            String json = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
            JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
            assertTrue(json.contains("\"remark\""));
            assertEquals(original, restored.toModelType());
        }
    }

    @Test
    public void jsonAdaptedPerson_labels_roundTripPreservesLabels() throws Exception {
        Person original = new PersonBuilder(BENSON).withLabels("Discrete Math Tutorial").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertTrue(json.contains("\"labels\""));
        assertEquals(original, restored.toModelType());
    }

    @Test
    public void jsonAdaptedPerson_attendance_roundTripPreservesRecords() throws Exception {
        AttendanceRecord record = new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        Person original = new PersonBuilder(BENSON).withAttendance(record).build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(original));

        assertTrue(json.contains("\"attendance\""));
        assertTrue(json.contains("\"date\" : \"2026-10-20\""));
        assertEquals(original, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void jsonAdaptedPerson_missingOrNullAttendance_defaultsToEmpty() throws Exception {
        String legacyJson = "{\"name\":\"" + VALID_NAME + "\",\"studentId\":\"" + VALID_STUDENT_ID
                + "\",\"email\":\"" + VALID_EMAIL + "\"}";
        Person expected = new JsonAdaptedPerson(BENSON).toModelType();
        assertEquals(expected, JsonUtil.fromJsonString(legacyJson, JsonAdaptedPerson.class).toModelType());
        String nullJson = legacyJson.substring(0, legacyJson.length() - 1) + ",\"attendance\":null}";
        assertEquals(expected, JsonUtil.fromJsonString(nullJson, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_invalidAttendance_throwsIllegalValueException() {
        List<JsonAdaptedAttendanceRecord> invalidDate = List.of(
                new JsonAdaptedAttendanceRecord("2026-02-30", "Tutorial 1"));
        JsonAdaptedPerson invalidDatePerson = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(), "", invalidDate);
        assertThrows(IllegalValueException.class, JsonAdaptedAttendanceRecord.MESSAGE_INVALID_DATE,
                invalidDatePerson::toModelType);

        List<JsonAdaptedAttendanceRecord> invalidGroup = List.of(
                new JsonAdaptedAttendanceRecord("2026-10-20", "Tutorial/1"));
        JsonAdaptedPerson invalidGroupPerson = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(), "", invalidGroup);
        assertThrows(IllegalValueException.class, Label.MESSAGE_CONSTRAINTS, invalidGroupPerson::toModelType);
    }

    @Test
    public void toModelType_duplicateAttendanceIgnoringCase_throwsIllegalValueException() {
        List<JsonAdaptedAttendanceRecord> records = List.of(
                new JsonAdaptedAttendanceRecord("2026-10-20", "İstanbul"),
                new JsonAdaptedAttendanceRecord("2026-10-20", "i\u0307stanbul"));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(), "", records);

        assertThrows(IllegalValueException.class, MESSAGE_DUPLICATE_ATTENDANCE, person::toModelType);
    }

    @Test
    public void toModelType_nullAttendanceEntry_throwsIllegalValueException() {
        List<JsonAdaptedAttendanceRecord> records = new ArrayList<>();
        records.add(null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                List.of(), "", records);

        assertThrows(IllegalValueException.class, MISSING_ATTENDANCE_MESSAGE, person::toModelType);
    }

    @Test
    public void jsonAdaptedPerson_missingOrNullRemark_defaultsToEmpty() throws Exception {
        String legacyJson = "{\"name\":\"" + VALID_NAME + "\",\"studentId\":\"" + VALID_STUDENT_ID
                + "\",\"email\":\"" + VALID_EMAIL + "\"}";
        Person expected = new PersonBuilder(BENSON).withRemark("").build();
        assertEquals(expected, JsonUtil.fromJsonString(legacyJson, JsonAdaptedPerson.class).toModelType());
        String nullRemarkJson = legacyJson.substring(0, legacyJson.length() - 1) + ",\"remark\":null}";
        assertEquals(expected, JsonUtil.fromJsonString(nullRemarkJson, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void jsonAdaptedPerson_missingOrNullLabels_defaultsToEmpty() throws Exception {
        String legacyJson = "{\"name\":\"" + VALID_NAME + "\",\"studentId\":\"" + VALID_STUDENT_ID
                + "\",\"email\":\"" + VALID_EMAIL + "\",\"remark\":\"\"}";
        Person expected = new PersonBuilder(BENSON).withRemark("").build();
        assertEquals(expected, JsonUtil.fromJsonString(legacyJson, JsonAdaptedPerson.class).toModelType());
        String nullLabelsJson = legacyJson.substring(0, legacyJson.length() - 1) + ",\"labels\":null}";
        assertEquals(expected, JsonUtil.fromJsonString(nullLabelsJson, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void jsonAdaptedPerson_legacyFields_ignoredAndNotSaved() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));
        String legacyJson = json.substring(0, json.lastIndexOf('}'))
                + ",\"phone\":\"98765432\",\"address\":\"Old address\",\"tags\":[\"friends\"]}";
        Person restored = JsonUtil.fromJsonString(legacyJson, JsonAdaptedPerson.class).toModelType();
        assertEquals(BENSON, restored);
        String savedJson = JsonUtil.toJsonString(new JsonAdaptedPerson(restored));
        assertFalse(savedJson.contains("\"phone\""));
        assertFalse(savedJson.contains("\"address\""));
        assertFalse(savedJson.contains("\"tags\""));
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void jsonAdaptedPerson_personWithLowercaseStudentId_serializesNormalizedStudentId() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(new PersonBuilder(BENSON)
                .withStudentId(VALID_STUDENT_ID.toLowerCase()).build());
        String jsonString = JsonUtil.toJsonString(person);

        assertTrue(jsonString.contains("\"studentId\" : \"" + VALID_STUDENT_ID + "\""));
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                        List.of(), "");
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_STUDENT_ID, VALID_EMAIL, List.of(), "");
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidStudentId_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_STUDENT_ID, VALID_EMAIL,
                        List.of(), "");
        String expectedMessage = StudentId.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullStudentId_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL,
                List.of(), "");
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, StudentId.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, INVALID_EMAIL,
                        List.of(), "");
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, null, List.of(), "");
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidLabels_throwsIllegalValueException() {
        List<JsonAdaptedLabel> invalidLabels = new ArrayList<>(VALID_LABELS);
        invalidLabels.add(new JsonAdaptedLabel(INVALID_LABEL));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                        invalidLabels, "");
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_duplicateLabelsIgnoringCase_throwsIllegalValueException() {
        List<JsonAdaptedLabel> duplicateLabels = List.of(
                new JsonAdaptedLabel("Tutorial 1"),
                new JsonAdaptedLabel("tutorial 1"));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                        duplicateLabels, "");

        assertThrows(IllegalValueException.class, MESSAGE_DUPLICATE_LABEL, person::toModelType);
    }

    @Test
    public void toModelType_nullLabelName_throwsIllegalValueException() {
        List<JsonAdaptedLabel> invalidLabels = new ArrayList<>(VALID_LABELS);

        // Cast selects the String constructor for this null input.
        invalidLabels.add(new JsonAdaptedLabel((String) null));

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                        invalidLabels, "");
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_nullLabel_throwsIllegalValueException() {
        List<JsonAdaptedLabel> invalidLabels = new ArrayList<>(VALID_LABELS);
        invalidLabels.add(null);
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_STUDENT_ID, VALID_EMAIL,
                        invalidLabels, "");
        assertThrows(IllegalValueException.class, MISSING_LABEL_MESSAGE, person::toModelType);
    }

}
