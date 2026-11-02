package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.DiagnosticLogCapture;
import seedu.address.ui.UiManager;

public class MainAppLifecycleTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void init_existingFiles_loadsStudentsAndPreferencesIntoComponents() throws Exception {
        Storage storage = createStorage();
        storage.saveAddressBook(getTypicalAddressBook());
        UserPrefs preferences = new UserPrefs();
        preferences.setGuiSettings(new GuiSettings(1000, 700, 150, 100));
        storage.saveUserPrefs(preferences);
        MainApp app = new TemporaryStorageApp(storage);

        app.init();

        assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
        assertEquals(preferences.getGuiSettings(), app.logic.getGuiSettings());
        assertEquals(getTypicalAddressBook().getPersonList(), app.logic.getFilteredPersonList());
        assertInstanceOf(UiManager.class, app.ui);
        app.logic.execute("add n/Samuel i/A0123456B e/sam@example.com");
        assertEquals(app.model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(getTypicalAddressBook().getPersonList().size() + 1,
                app.logic.getFilteredPersonList().size());
    }

    @Test
    public void init_missingFiles_createsDefaultPreferencesAndEmptyModel() throws Exception {
        Storage storage = createStorage();
        MainApp app = new TemporaryStorageApp(storage);

        app.init();

        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
        assertEquals(MainApp.MESSAGE_NO_EXISTING_DATA, app.logic.getStartupMessage());
        assertEquals(0, app.logic.getFilteredPersonList().size());
        assertInstanceOf(UiManager.class, app.ui);
    }

    @Test
    public void start_delegatesToUiOnce() {
        MainApp app = new MainApp();
        AtomicInteger startCalls = new AtomicInteger();
        // A UI stub avoids requiring a JavaFX window for this lifecycle delegation test.
        app.ui = stage -> {
            assertNull(stage);
            startCalls.incrementAndGet();
        };

        app.start(null);

        assertEquals(1, startCalls.get());
    }

    @Test
    public void stop_updatedPreferences_savesSettingsAndPreservesStudents() throws Exception {
        Storage storage = createStorage();
        storage.saveAddressBook(getTypicalAddressBook());
        String originalData = Files.readString(storage.getAddressBookFilePath());
        MainApp app = new TemporaryStorageApp(storage);
        app.init();
        GuiSettings updatedSettings = new GuiSettings(1100, 750, 50, 60);
        app.logic.setGuiSettings(updatedSettings);

        app.stop();

        assertEquals(updatedSettings, storage.readUserPrefs().orElseThrow().getGuiSettings());
        assertEquals(originalData, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void initPrefs_saveFails_keepsDefaultsAndLogsOnlyExceptionType() {
        MainApp app = new MainApp();
        try (DiagnosticLogCapture logs = new DiagnosticLogCapture()) {
            UserPrefs prefs = app.initPrefs(createFailingPreferencesStorage());

            assertEquals(new UserPrefs(), prefs);
            assertTrue(logs.getOutput().contains("Failed to save preference file: IOException"));
            assertFalse(logs.getOutput().contains("PrivatePreferenceMarker"));
        }
    }

    @Test
    public void stop_preferencesSaveFails_preservesModelAndLogsOnlyExceptionType() {
        MainApp app = new MainApp();
        app.model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        app.storage = new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                createFailingPreferencesStorage());
        try (DiagnosticLogCapture logs = new DiagnosticLogCapture()) {
            app.stop();

            assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
            assertTrue(logs.getOutput().contains("Failed to save preferences: IOException"));
            assertFalse(logs.getOutput().contains("PrivatePreferenceMarker"));
        }
    }

    private JsonUserPrefsStorage createFailingPreferencesStorage() {
        return new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                throw new IOException("PrivatePreferenceMarker");
            }
        };
    }

    private Storage createStorage() {
        return new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    /**
     * Runs the real application lifecycle while redirecting student data and preferences to temporary storage.
     */
    private static class TemporaryStorageApp extends MainApp {
        private final Storage temporaryStorage;

        TemporaryStorageApp(Storage temporaryStorage) {
            this.temporaryStorage = temporaryStorage;
        }

        @Override
        protected UserPrefs initPrefs(JsonUserPrefsStorage ignoredStorage) {
            return super.initPrefs(new JsonUserPrefsStorage(temporaryStorage.getUserPrefsFilePath()));
        }

        @Override
        protected Model initModelManager(Storage ignoredStorage, ReadOnlyUserPrefs preferences) {
            storage = temporaryStorage;
            return super.initModelManager(temporaryStorage, preferences);
        }
    }
}
