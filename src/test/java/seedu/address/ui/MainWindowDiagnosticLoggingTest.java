package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.DiagnosticLogCapture;
import seedu.address.testutil.FxTestUtil;

public class MainWindowDiagnosticLoggingTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void enterCommands_displaysPrivateFeedbackWithoutLoggingIt() throws Exception {
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        try (DiagnosticLogCapture logs = new DiagnosticLogCapture()) {
            FxTestUtil.runAndWait(() -> {
                Stage stage = new Stage();
                try {
                    MainWindow window = new MainWindow(stage, logic, temporaryFolder.resolve("students.json"));
                    window.fillInnerParts();
                    TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                    TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                    String add = "add n/PrivateStudentName i/A2345678C e/private@example.com r/PrivateRemark";

                    input.setText(add);
                    input.fireEvent(new ActionEvent());
                    assertTrue(feedback.getText().contains("PrivateStudentName"));
                    assertTrue(feedback.getText().contains("PrivateRemark"));
                    assertTrue(input.getText().isEmpty());

                    input.setText(add);
                    input.fireEvent(new ActionEvent());
                    assertTrue(feedback.getText().contains("A2345678C"));
                    assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                    input.setText("PrivateUnknownCommand PrivateRemark");
                    input.fireEvent(new ActionEvent());
                    assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                } finally {
                    stage.close();
                }
            });

            String output = logs.getOutput();
            assertTrue(output.contains("Command completed successfully."));
            assertTrue(output.contains("Command failed: CommandException"));
            assertTrue(output.contains("Command failed: ParseException"));
            for (String value : new String[] {"PrivateStudentName", "A2345678C", "private@example.com",
                "PrivateRemark", "PrivateUnknownCommand"}) {
                assertFalse(output.contains(value), "Diagnostic logs exposed " + value);
            }
        }
    }
}
