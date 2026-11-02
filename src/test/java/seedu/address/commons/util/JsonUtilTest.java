package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void readJsonFile_nullJson_throwsDataLoadingExceptionAndPreservesFile() throws Exception {
        Path file = temporaryFolder.resolve("null.json");
        Files.writeString(file, "null");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));
        assertEquals("null", Files.readString(file));
    }

    @Test
    public void readJsonFile_missingFile_returnsEmpty() throws Exception {
        assertTrue(JsonUtil.readJsonFile(temporaryFolder.resolve("missing.json"),
                SerializableTestClass.class).isEmpty());
    }

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void fromJsonString_trailingContent_throwsIoException() {
        String json = SerializableTestClass.JSON_STRING_REPRESENTATION;
        for (String suffix : List.of(" garbage", " {}", " []", " null", " true", " 42", " \"text\"",
                " ]", " }", " {\"unfinished\":")) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json + suffix, SerializableTestClass.class));
        }
    }

    @Test
    public void fromJsonString_duplicateKeys_throwsIoException() {
        for (String json : List.of(
                "{\"name\":\"Alex\",\"name\":\"Beth\"}",
                "{\"mapOfIntegerToString\":{\"1\":\"One\",\"1\":\"Two\"}}",
                "{\"unknown\":{\"name\":\"Alex\",\"name\":\"Beth\"}}",
                "{\"name\":\"Alex\",\"\\u006eame\":\"Beth\"}")) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, SerializableTestClass.class));
        }
    }

    @Test
    public void fromJsonString_nonStringValues_throwsIoException() {
        for (String value : List.of("true", "false", "123", "1.5", "[]", "{}")) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(
                    "{\"name\":" + value + "}", SerializableTestClass.class));
        }
    }

    @Test
    public void fromJsonString_validWhitespaceAndSeparateObjects_success() throws IOException {
        String json = " \n\t{\"name\":\"Alex \\\"Tan\\\"\","
                + "\"unknown\":[{\"name\":\"Beth\"},{\"name\":\"Chris\"}]}\r\n\t ";

        assertEquals("Alex \"Tan\"", JsonUtil.fromJsonString(json, SerializableTestClass.class).getName());
    }

    @Test
    public void fromJsonString_surroundingWhitespace_success() throws IOException {
        String json = " \t\r\n" + SerializableTestClass.JSON_STRING_REPRESENTATION + " \t\r\n";
        SerializableTestClass result = JsonUtil.fromJsonString(json, SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), result.getName());
        assertEquals(SerializableTestClass.getListTestValues(), result.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), result.getMapOfIntegerToString());
    }

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
