package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.AppPaths;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.6";
    public static final String MESSAGE_STUDENTS_LOADED = "%1$d students loaded.";
    public static final String MESSAGE_ONE_STUDENT_LOADED = "1 student loaded.";
    public static final String MESSAGE_NO_EXISTING_DATA =
            "No existing student data found. Starting with an empty student list.";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = AppPaths.getPreferencesFilePath();
    private static final Path ADDRESS_BOOK_FILE_PATH = AppPaths.getStudentDataFilePath();

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    private boolean isStudentDataAvailable = true;
    private String startupMessage = "";

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing TeachAssist ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = initLogic(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * Starts empty if the data file is absent or unreadable. A loading failure also blocks modifying commands.
     * Records the outcome of loading as the startup message shown to the user.
     *
     * @param storage The storage from which to load student data.
     * @param userPrefs The initial user preferences.
     * @return The initial student model.
     */
    protected Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                startupMessage = MESSAGE_NO_EXISTING_DATA;
            } else {
                startupMessage = formatStudentsLoaded(addressBookOptional.get().getPersonList().size());
            }
            logger.info(startupMessage);
            initialData = addressBookOptional.orElseGet(AddressBook::new);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty student list.");
            initialData = new AddressBook();
            isStudentDataAvailable = false;
            startupMessage = e.getMessage();
        }

        return new ModelManager(initialData, userPrefs);
    }

    private static String formatStudentsLoaded(int studentCount) {
        return studentCount == 1 ? MESSAGE_ONE_STUDENT_LOADED : String.format(MESSAGE_STUDENTS_LOADED, studentCount);
    }

    /**
     * Creates the logic component using the result of startup student-data loading.
     *
     * @param model The initial student model.
     * @param storage The student-data storage.
     * @return Logic that blocks modifications if startup loading failed and reports the loading outcome.
     */
    protected Logic initLogic(Model model, Storage storage) {
        return new LogicManager(model, storage, isStudentDataAvailable, startupMessage);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file: " + e.getClass().getSimpleName());
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting TeachAssist " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping TeachAssist ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences: " + e.getClass().getSimpleName());
        }
    }
}
