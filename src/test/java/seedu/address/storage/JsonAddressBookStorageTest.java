package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.attendance.AttendanceRecord;
import seedu.address.model.label.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.DiagnosticLogCapture;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_legacyExpandedLabel_survivesSaveAndReload() throws Exception {
        Path file = testFolder.resolve("legacy.json");
        String group = "İ".repeat(60);
        Files.writeString(file, """
                {"persons":[{"name":"Alex Tan","studentId":"A0123456B","email":"alex@example.com",
                "labels":["%s"],"attendance":[{"date":"2026-10-20","group":"%s"}]}]}
                """.formatted(group, group));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        Person student = loaded.getPersonList().get(0);
        Label expected = new Label(group);
        assertTrue(student.getLabels().contains(expected));
        assertTrue(student.getAttendance().contains(
                new AttendanceRecord(LocalDate.parse("2026-10-20"), expected)));
        storage.saveAddressBook(loaded);
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertEquals(loaded, reloaded);
        assertTrue(Label.isValidLabelName(expected.labelName));
    }

    @Test
    public void saveAddressBook_replacementFailure_preservesFileAndRemovesTemporaryFile() throws Exception {
        Path file = testFolder.resolve("existing.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        AddressBook original = getTypicalAddressBook();
        storage.saveAddressBook(original);
        String originalText = Files.readString(file);
        JsonAddressBookStorage failingStorage = replacementFailingStorage(file);
        failingStorage.readAddressBook();
        assertThrows(IOException.class, () -> failingStorage.saveAddressBook(new AddressBook()));
        assertEquals(originalText, Files.readString(file));
        assertEquals(original, storage.readAddressBook().orElseThrow());
        try (Stream<Path> files = Files.list(testFolder)) {
            assertEquals(0, files.filter(path -> path.toString().endsWith(".tmp")).count());
        }
    }

    @Test
    public void saveAddressBook_cleanupFails_preservesOriginalFailureAndDataWithoutLoggingStudentValues()
            throws Exception {
        Path file = testFolder.resolve("existing.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        String original = Files.readString(file);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
            @Override
            protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
                // A nonempty directory makes cleanup fail deterministically on every CI platform.
                Files.delete(temporaryFile);
                Files.createDirectory(temporaryFile);
                Files.writeString(temporaryFile.resolve("obstruction"), "PrivateStudentMarker");
                throw new IOException("PrivateStudentMarker");
            }
        };
        failingStorage.readAddressBook();

        try (DiagnosticLogCapture logs = new DiagnosticLogCapture()) {
            assertThrows(IOException.class, "PrivateStudentMarker", () ->
                    failingStorage.saveAddressBook(new AddressBook()));

            assertEquals(original, Files.readString(file));
            assertTrue(logs.getOutput().contains("Unable to remove temporary data file"));
            assertTrue(logs.getOutput().contains("DirectoryNotEmptyException"));
            assertFalse(logs.getOutput().contains("PrivateStudentMarker"));
        }
    }

    @Test
    public void saveAddressBook_firstSaveFails_doesNotCreateEmptyDataFile() throws Exception {
        Path file = testFolder.resolve("new.json");
        assertThrows(IOException.class, () -> replacementFailingStorage(file).saveAddressBook(new AddressBook()));
        assertFalse(Files.exists(file));
        try (Stream<Path> files = Files.list(testFolder)) {
            assertEquals(0, files.filter(path -> path.toString().endsWith(".tmp")).count());
        }
    }

    @Test
    public void saveAddressBook_folderCannotBeCreated_throwsDataStorageLocationException() throws Exception {
        Path blockingFile = Files.writeString(testFolder.resolve("data"), "not a folder");
        Path file = blockingFile.resolve("addressBook.json");
        assertThrows(DataStorageLocationException.class, () ->
                new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook()));
        assertEquals("not a folder", Files.readString(blockingFile));
    }

    @Test
    public void saveAddressBook_missingParentDirectories_createsThem() throws Exception {
        Path file = testFolder.resolve("nested/data/students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        assertTrue(Files.isRegularFile(file));
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private JsonAddressBookStorage replacementFailingStorage(Path file) {
        return new JsonAddressBookStorage(file) {
            @Override
            protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
                assertTrue(Files.size(temporaryFile) > 0);
                throw new IOException("Simulated replacement failure");
            }
        };
    }

    @Test
    public void readAndSaveAddressBook_paddedFields_savesTrimmedValues() throws Exception {
        // EP: Unicode padding is removed; internal Unicode spaces in remarks are preserved.
        Path file = testFolder.resolve("padded.json");
        String originalContents = """
                {"persons":[{
                  "name":"\u00a0Alice\u2009\u2009Pauline\u3000",
                  "studentId":"\u202fa1234567b\u00a0",
                  "email":"\u2009Alice@EXAMPLE.COM\u3000",
                  "remark":"\u00a0Follow\u2009up\u202f\u202fsoon\\nNext week\u3000",
                  "labels":["\u3000Tutorial\u00a0\u00a01\u202f"],
                  "attendance":[{"date":"\u00a02026-10-20\u3000","group":"\u202fTutorial\u2009\u20091\u00a0"}]
                }]}
                """;
        Files.writeString(file, originalContents);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
        AddressBook expected = new AddressBook();
        expected.addPerson(new PersonBuilder(ALICE).withName("Alice Pauline").withStudentId("A1234567B")
                .withEmail("Alice@example.com").withRemark("Follow\u2009up\u202f\u202fsoon\nNext week")
                .withLabels("Tutorial\u00a0\u00a01")
                .withAttendance(new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial\u2009\u20091")))
                .build());

        assertEquals(expected, restored);
        assertEquals(originalContents, Files.readString(file));
        storage.saveAddressBook(restored);
        assertEquals(JsonUtil.toJsonString(new JsonSerializableAddressBook(expected)), Files.readString(file));
        assertEquals(expected, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_withRemark_preservesRemark() throws Exception {
        Path filePath = testFolder.resolve("RemarksAddressBook.json");
        Person person = new PersonBuilder(ALICE).withRemark("Needs help with recursion\nFollow up next week").build();
        AddressBook original = new AddressBook();
        original.addPerson(person);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);
        ReadOnlyAddressBook restored = new JsonAddressBookStorage(filePath).readAddressBook().get();

        assertEquals(original, new AddressBook(restored));
        assertEquals(person.getRemark(), restored.getPersonList().get(0).getRemark());
    }

    @Test
    public void readAndSaveAddressBook_withUnicodeRemark_preservesInternalSpaces() throws Exception {
        Path file = testFolder.resolve("UnicodeRemarksAddressBook.json");
        String remark = "Meet\u00a0at 2pm\nFollow\u2009up\u202f\u202fsoon\u3000please";
        Person person = new PersonBuilder(ALICE).withRemark(remark).withLabels("Tutorial\u00a0\u20091")
                .withAttendance(new AttendanceRecord(LocalDate.parse("2026-10-20"),
                        new Label("Tutorial\u00a0\u20091"))).build();
        AddressBook original = new AddressBook();
        original.addPerson(person);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

        storage.saveAddressBook(original);
        ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
        assertEquals(remark, restored.getPersonList().get(0).getRemark().value);
        assertEquals(original, restored);

        storage.saveAddressBook(restored);
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertEquals(remark, reloaded.getPersonList().get(0).getRemark().value);
        assertEquals(original, reloaded);
    }

    @Test
    public void readAndSaveAddressBook_withLabel_preservesLabel() throws Exception {
        Path filePath = testFolder.resolve("LabelsAddressBook.json");
        Person person = new PersonBuilder(ALICE).withLabels("Discrete Math Tutorial").build();
        AddressBook original = new AddressBook();
        original.addPerson(person);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);
        ReadOnlyAddressBook restored = new JsonAddressBookStorage(filePath).readAddressBook().get();

        assertEquals(original, new AddressBook(restored));
        assertEquals(person.getLabels(), restored.getPersonList().get(0).getLabels());
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, () ->
                readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, () ->
                readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, () ->
                readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_duplicateStudentId_throwsDuplicateStudentIdMessage() {
        Path file = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest",
                "duplicatePersonAddressBook.json");
        assertThrows(DataLoadingException.class,
                String.format(JsonSerializableAddressBook.MESSAGE_DUPLICATE_STUDENT_ID, "A1234567B"), () ->
                new JsonAddressBookStorage(file).readAddressBook());
    }

    @Test
    public void readAddressBook_nullJson_throwsInvalidFileMessageAndPreservesFile() throws Exception {
        Path file = testFolder.resolve("nullAddressBook.json");
        String originalContents = "  null\n";
        Files.writeString(file, originalContents);

        assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, ()
            -> new JsonAddressBookStorage(file).readAddressBook());
        assertEquals(originalContents, Files.readString(file));
    }

    @Test
    public void readAddressBook_trailingContent_throwsInvalidFileMessageAndPreservesFile() throws Exception {
        Path file = testFolder.resolve("trailingContent.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        String validJson = Files.readString(file);

        for (String suffix : new String[] {" garbage", " {\"persons\": []}"}) {
            String contents = validJson + suffix;
            Files.writeString(file, contents);

            assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, () ->
                    storage.readAddressBook());
            assertEquals(contents, Files.readString(file));
        }
    }

    @Test
    public void readAddressBook_wrongJsonStructure_throwsInvalidFileMessage() throws Exception {
        Path file = testFolder.resolve("wrongStructure.json");
        Files.writeString(file, "{ \"persons\": \"not a list\" }");
        assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, () ->
                new JsonAddressBookStorage(file).readAddressBook());
    }

    @Test
    public void readAddressBook_malformedUtf8_rejectsAndPreservesBytes() throws Exception {
        Path file = testFolder.resolve("malformedUtf8.json");
        byte[] prefix = ("{\"persons\":[{\"name\":\"Alex Tan\",\"studentId\":\"A0123456B\","
                + "\"email\":\"alex@example.com\",\"remark\":\"").getBytes(StandardCharsets.UTF_8);
        byte[] suffix = "\"}]}".getBytes(StandardCharsets.UTF_8);
        // Invalid leading byte, invalid continuation, truncated sequence, overlong encoding, and surrogate.
        for (byte[] invalid : List.of(new byte[] {(byte) 0xff}, new byte[] {(byte) 0xc3, 0x28},
                new byte[] {(byte) 0xe2, (byte) 0x82}, new byte[] {(byte) 0xc0, (byte) 0xaf},
                new byte[] {(byte) 0xed, (byte) 0xa0, (byte) 0x80})) {
            byte[] contents = ByteBuffer.allocate(prefix.length + invalid.length + suffix.length)
                    .put(prefix).put(invalid).put(suffix).array();
            Files.write(file, contents);

            assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, () ->
                    new JsonAddressBookStorage(file).readAddressBook());
            assertArrayEquals(contents, Files.readAllBytes(file));
        }
    }

    @Test
    public void readAddressBook_validUtf8_preservesUnicodeAndReplacementCharacter() throws Exception {
        Path file = testFolder.resolve("validUtf8.json");
        String remark = "\u4e2d\u6587 \u00e9 \uD83D\uDE00 \uFFFD";
        Map<String, Object> student = validStudentFields();
        student.put("remark", remark);
        Files.writeString(file, JsonUtil.toJsonString(Map.of("persons", List.of(student))));

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertEquals(remark, loaded.getPersonList().getFirst().getRemark().value);
    }

    @Test
    public void readAddressBook_unpairedSurrogateEscapes_rejectsAndPreservesFile() throws Exception {
        for (String escaped : List.of("\\uD800", "\\uDC00", "\\uD800\\uD800", "\\uDC00\\uD800",
                "before\\uD800after", "\\uD800\\u0041", "\\uD83D\\uDE00\\uD800")) {
            assertInvalidFilePreserved("{\"persons\":[{\"name\":\"Alex Tan\",\"studentId\":\"A0123456B\","
                    + "\"email\":\"alex@example.com\",\"remark\":\"" + escaped + "\"}]}");
        }
    }

    @Test
    public void readAddressBook_validSurrogatePairEscape_survivesSaveAndReload() throws Exception {
        Path file = testFolder.resolve("surrogatePair.json");
        Files.writeString(file, "{\"persons\":[{\"name\":\"Alex Tan\",\"studentId\":\"A0123456B\","
                + "\"email\":\"alex@example.com\",\"remark\":\"\\uD83D\\uDE00\"}]}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals("\uD83D\uDE00", loaded.getPersonList().getFirst().getRemark().value);

        storage.saveAddressBook(loaded);
        assertEquals(loaded, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    public void readAddressBook_trailingContent_rejectsAndPreservesFile() throws Exception {
        String validJson = JsonUtil.toJsonString(new JsonSerializableAddressBook(getTypicalAddressBook()));
        for (String suffix : List.of("garbage", "{}", "{\"persons\":[]}", "null", "42")) {
            assertInvalidFilePreserved(validJson + "\n" + suffix);
        }
    }

    @Test
    public void readAddressBook_duplicateKeys_rejectsAndPreservesFile() throws Exception {
        String student = JsonUtil.toJsonString(validStudentFields());
        assertInvalidFilePreserved("{\"persons\":[" + student + "],\"persons\":[]}");
        assertInvalidFilePreserved("{\"persons\":[" + student.substring(0, student.length() - 1)
                + ",\"studentId\":\"A9999999Z\"}]}");
        assertInvalidFilePreserved("{\"persons\":[" + student.substring(0, student.length() - 1)
                + ",\"attendance\":[{\"date\":\"2026-10-20\",\"group\":\"Tutorial 1\","
                + "\"group\":\"Tutorial 2\"}]}]}");
    }

    @Test
    public void readAddressBook_nonStringStudentFields_rejectsAndPreservesFile() throws Exception {
        for (String field : List.of("name", "studentId", "email", "remark")) {
            for (Object value : List.of(true, false, 123456789, 1.5, List.of("text"), Map.of("value", "text"))) {
                Map<String, Object> student = validStudentFields();
                student.put(field, value);
                assertInvalidFilePreserved(JsonUtil.toJsonString(Map.of("persons", List.of(student))));
            }
        }
    }

    @Test
    public void readAddressBook_nonStringLabelsAndAttendance_rejectsAndPreservesFile() throws Exception {
        for (Object value : List.of(true, false, 123, 1.5, List.of("text"), Map.of("value", "text"))) {
            Map<String, Object> student = validStudentFields();
            student.put("labels", List.of(value));
            assertInvalidFilePreserved(JsonUtil.toJsonString(Map.of("persons", List.of(student))));

            for (String field : List.of("date", "group")) {
                student = validStudentFields();
                Map<String, Object> record = new HashMap<>(Map.of("date", "2026-10-20", "group", "Tutorial 1"));
                record.put(field, value);
                student.put("attendance", List.of(record));
                assertInvalidFilePreserved(JsonUtil.toJsonString(Map.of("persons", List.of(student))));
            }
        }
    }

    @Test
    public void readAddressBook_legacyOptionalFieldsAndUnknownProperties_success() throws Exception {
        Path file = testFolder.resolve("legacy.json");
        for (String optionalFields : List.of("", ",\"remark\":null,\"labels\":null,\"attendance\":null")) {
            String contents = " \n{\"persons\":[{\"name\":\"Alex Tan\",\"studentId\":\"A0123456B\","
                    + "\"email\":\"alex@example.com\",\"tags\":[\"legacy\"]" + optionalFields + "}]}\t\n";
            Files.writeString(file, contents);
            ReadOnlyAddressBook restored = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();

            assertEquals(1, restored.getPersonList().size());
            Person student = restored.getPersonList().getFirst();
            assertEquals("Alex Tan", student.getName().fullName);
            assertEquals("", student.getRemark().value);
            assertTrue(student.getLabels().isEmpty());
            assertTrue(student.getAttendance().isEmpty());
            assertEquals(contents, Files.readString(file));
        }
    }

    private Map<String, Object> validStudentFields() {
        return new HashMap<>(Map.of("name", "Alex Tan", "studentId", "A0123456B",
                "email", "alex@example.com", "remark", "Needs practice", "labels", List.of("Tutorial 1")));
    }

    private void assertInvalidFilePreserved(String contents) throws Exception {
        Path file = testFolder.resolve("invalid.json");
        Files.writeString(file, contents);

        assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_INVALID_FILE, () ->
                new JsonAddressBookStorage(file).readAddressBook());
        assertEquals(contents, Files.readString(file));
    }

    @Test
    public void readAddressBook_unreadableFile_throwsUnreadableFileMessage() throws Exception {
        Path directory = Files.createDirectory(testFolder.resolve("addressBook.json"));
        assertThrows(DataLoadingException.class, JsonAddressBookStorage.MESSAGE_UNREADABLE_FILE, () ->
                new JsonAddressBookStorage(directory).readAddressBook());
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
