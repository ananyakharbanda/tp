package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class DiagnosticLoggingTest {
    private static final String NAME = "PrivateStudentName";
    private static final String STUDENT_ID = "A2345678C";
    private static final String EMAIL = "private-marker@example.com";
    private static final String REMARK = "SensitiveRemarkMarker";
    private static final String GROUP = "PrivateGroup";
    private static final String UNKNOWN_COMMAND = "PrivateUnknownWord";

    @TempDir
    public Path temporaryFolder;

    private final List<String> messages = new ArrayList<>();
    private Logger applicationLogger;
    private Level previousLevel;
    private Handler handler;

    @BeforeEach
    public void captureAllDiagnosticLevels() {
        applicationLogger = LogsCenter.getLogger(DiagnosticLoggingTest.class).getParent();
        previousLevel = applicationLogger.getLevel();
        handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(new SimpleFormatter().format(record));
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        handler.setLevel(Level.ALL);
        applicationLogger.addHandler(handler);
        applicationLogger.setLevel(Level.ALL);
    }

    @AfterEach
    public void restoreLogging() {
        applicationLogger.removeHandler(handler);
        applicationLogger.setLevel(previousLevel);
    }

    @Test
    public void execute_successesAndFailures_keepsStudentDetailsOnlyInUserFeedback() throws Exception {
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        String add = "add n/" + NAME + " i/" + STUDENT_ID + " e/" + EMAIL + " r/" + REMARK + " l/" + GROUP;
        String feedback = logic.execute(add).getFeedbackToUser();
        assertTrue(feedback.contains(NAME));
        assertTrue(feedback.contains(REMARK));
        assertThrows(CommandException.class, () -> logic.execute(add));
        logic.execute("edit i/" + STUDENT_ID + " r/" + REMARK);
        assertThrows(CommandException.class, () -> logic.execute("edit i/" + STUDENT_ID + " l/" + GROUP));
        assertThrows(CommandException.class, () -> logic.execute("edit i/" + STUDENT_ID + " dl/MissingPrivateGroup"));
        String mark = "mark i/" + STUDENT_ID + " d/2026-10-20 l/" + GROUP;
        logic.execute(mark);
        assertThrows(CommandException.class, () -> logic.execute(mark));
        logic.execute("attendance d/2026-10-20 l/" + GROUP);
        String unmark = "unmark i/" + STUDENT_ID + " d/2026-10-20 l/" + GROUP;
        logic.execute(unmark);
        assertThrows(CommandException.class, () -> logic.execute(unmark));
        logic.execute("find r/" + REMARK);
        assertThrows(ParseException.class, () -> logic.execute(UNKNOWN_COMMAND + " " + REMARK));
        logic.execute("delete i/" + STUDENT_ID);
        assertThrows(CommandException.class, () -> logic.execute("delete i/" + STUDENT_ID));

        assertPrivateDetailsAbsent();
        assertTrue(messages.stream().anyMatch(message -> message.contains("Add rejected")));
        assertTrue(messages.stream().anyMatch(message -> message.contains("Marked attendance")));
    }

    @Test
    public void read_invalidAndDuplicateStudentData_logsFailureWithoutJsonValues() throws Exception {
        Path file = temporaryFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Files.writeString(file, "{\"persons\":\"" + REMARK + "\"}");
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        String student = "{\"name\":\"" + NAME + "\",\"studentId\":\"" + STUDENT_ID
                + "\",\"email\":\"" + EMAIL + "\",\"remark\":\"" + REMARK + "\",\"labels\":[\"" + GROUP + "\"]}";
        Files.writeString(file, "{\"persons\":[" + student + "," + student + "]}");
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Files.writeString(file, "{\"persons\":[" + student.replace(EMAIL, "PrivateInvalidEmail") + "]}");
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        assertPrivateDetailsAbsent();
        assertTrue(messages.stream().anyMatch(message -> message.contains("Duplicate")));
    }

    private void assertPrivateDetailsAbsent() {
        String output = String.join("\n", messages);
        for (String privateValue : List.of(NAME, STUDENT_ID, EMAIL, REMARK, GROUP, UNKNOWN_COMMAND,
                "PrivateInvalidEmail", "2026-10-20")) {
            assertFalse(output.contains(privateValue), "Diagnostic logs exposed " + privateValue);
        }
    }
}
