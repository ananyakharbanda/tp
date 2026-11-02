package seedu.address.logic;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AttendanceCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.attendance.AttendanceReport;
import seedu.address.model.person.Person;
import seedu.address.storage.DataFileChangedException;
import seedu.address.storage.DataFileInUseException;
import seedu.address.storage.DataStorageLocationException;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_SAVE_FAILURE = "Unable to save student data.";
    public static final String MESSAGE_STORAGE_LOCATION_FAILURE = "Unable to create data storage location.";
    public static final String MESSAGE_DATA_CHANGED =
            "Student data has changed outside this window. Restart TeachAssist to load the latest data before editing.";
    public static final String MESSAGE_DATA_IN_USE =
            "Student data is being saved by another window. Please try again.";
    public static final String MESSAGE_DATA_UNAVAILABLE =
            "Student data is unavailable because loading failed. Fix the data file and restart.";

    static final int MAX_UNDO_HISTORY_SIZE = 100;

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final boolean isStudentDataAvailable;
    private final String startupMessage;
    private final Deque<AddressBook> undoHistory = new ArrayDeque<>();

    /** The attendance report shown in place of the student list, or null while the student list is shown. */
    private AttendanceReport displayedAttendance;

    /**
     * Constructs a {@code LogicManager} whose student data is available for modification.
     *
     * @param model The live model.
     * @param storage The storage used for saving changes.
     * @throws NullPointerException if any parameter is null.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, true, "");
    }

    /**
     * Constructs a {@code LogicManager}, blocking modifications if startup loading failed.
     *
     * @param model The live model.
     * @param storage The storage used for saving changes.
     * @param isStudentDataAvailable Whether startup loading succeeded or no data file existed.
     * @param startupMessage The message describing the outcome of startup loading.
     * @throws NullPointerException if {@code model}, {@code storage}, or {@code startupMessage} is null.
     */
    public LogicManager(Model model, Storage storage, boolean isStudentDataAvailable, String startupMessage) {
        requireAllNonNull(model, storage, startupMessage);
        this.model = model;
        this.storage = storage;
        this.isStudentDataAvailable = isStudentDataAvailable;
        this.startupMessage = startupMessage;
        addressBookParser = new AddressBookParser();
    }

    /**
     * {@inheritDoc}
     * Executes modifying commands against a temporary model and publishes their changes only after saving succeeds.
     * Commands that do not modify student data execute without saving. Startup loading failures block modifications,
     * listing, finding students, and viewing attendance reports.
     * An open attendance report is refreshed after attendance changes and closed by other successful commands.
     */
    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        requireNonNull(commandText);
        logger.info("Processing command.");

        Command command = addressBookParser.parseCommand(commandText);
        logger.fine("Executing " + command.getClass().getSimpleName() + ".");
        if (!isStudentDataAvailable
                && (command.isModifyingData() || command instanceof ListCommand || command instanceof FindCommand
                        || command instanceof AttendanceCommand)) {
            throw new CommandException(MESSAGE_DATA_UNAVAILABLE);
        }
        if (!command.isModifyingData()) {
            return updateDisplayedAttendance(command.execute(model));
        }
        if (command instanceof UndoCommand) {
            return updateDisplayedAttendance(executeUndo());
        }
        return updateDisplayedAttendance(executeAndSave(command));
    }

    private CommandResult updateDisplayedAttendance(CommandResult result) {
        requireNonNull(result);
        if (result.getAttendanceReport().isPresent()) {
            displayedAttendance = result.getAttendanceReport().get();
            return result;
        }
        if (result.isAttendanceUpdated() && displayedAttendance != null) {
            displayedAttendance = AttendanceReport.of(model.getAddressBook().getPersonList(),
                    displayedAttendance.getDate(), displayedAttendance.getGroup());
            return result.withAttendanceReport(displayedAttendance);
        }
        displayedAttendance = null;
        return result;
    }

    private CommandResult executeAndSave(Command command) throws CommandException {
        requireNonNull(command);
        AddressBook previousAddressBook = new AddressBook(model.getAddressBook());
        Model pendingModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        pendingModel.updateFilteredPersonList(model.getFilteredPersonListPredicate());
        CommandResult result = command.execute(pendingModel);
        saveStudentData(pendingModel.getAddressBook());
        recordUndoSnapshot(previousAddressBook, pendingModel.getAddressBook());
        model.setAddressBook(pendingModel.getAddressBook());
        model.updateFilteredPersonList(pendingModel.getFilteredPersonListPredicate());
        return result;
    }

    /**
     * Retains {@code previousAddressBook} for undo, unless the command left the student records unchanged.
     */
    private void recordUndoSnapshot(AddressBook previousAddressBook, ReadOnlyAddressBook updatedAddressBook) {
        requireAllNonNull(previousAddressBook, updatedAddressBook);
        if (previousAddressBook.equals(updatedAddressBook)) {
            return;
        }
        if (undoHistory.size() == MAX_UNDO_HISTORY_SIZE) {
            undoHistory.removeFirst();
        }
        undoHistory.addLast(previousAddressBook);
    }

    /**
     * Saves the latest retained snapshot before replacing the live address book and consuming that snapshot.
     */
    private CommandResult executeUndo() throws CommandException {
        if (undoHistory.isEmpty()) {
            logger.info("Undo skipped: no history available.");
            return new CommandResult(UndoCommand.MESSAGE_NOTHING_TO_UNDO);
        }

        AddressBook previousAddressBook = undoHistory.getLast();
        saveStudentData(previousAddressBook);
        model.setAddressBook(previousAddressBook);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        undoHistory.removeLast();
        logger.info("Undo restored previous snapshot.");
        return new CommandResult(UndoCommand.MESSAGE_SUCCESS);
    }

    private void saveStudentData(ReadOnlyAddressBook addressBook) throws CommandException {
        requireNonNull(addressBook);
        try {
            storage.saveAddressBook(addressBook);
        } catch (DataFileChangedException e) {
            throw new CommandException(MESSAGE_DATA_CHANGED, e);
        } catch (DataFileInUseException e) {
            throw new CommandException(MESSAGE_DATA_IN_USE, e);
        } catch (DataStorageLocationException dsle) {
            logger.warning("Unable to create data storage location: " + dsle.getClass().getSimpleName());
            throw new CommandException(MESSAGE_STORAGE_LOCATION_FAILURE, dsle);
        } catch (IOException ioe) {
            logger.warning("Unable to save student data: " + ioe.getClass().getSimpleName());
            throw new CommandException(MESSAGE_SAVE_FAILURE, ioe);
        }
    }

    @Override
    public String getStartupMessage() {
        return startupMessage;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
