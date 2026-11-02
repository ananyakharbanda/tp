package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.STUDENT_ID_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.AttendanceRecord;
import seedu.address.model.attendance.AttendanceReport;
import seedu.address.model.label.Label;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.storage.DataFileInUseException;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;
    private StorageManager storage;

    // @@author ananyakharbanda
    @Test
    public void execute_staleInstance_preservesNewerFileModelFilterAndHistory() throws Exception {
        AttendanceRecord record = new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        model.addPerson(new PersonBuilder(ALICE).withLabels("Tutorial 1").withAttendance(record).build());
        model.addPerson(BENSON);
        storage.saveAddressBook(model.getAddressBook());
        StorageManager staleStorage = new StorageManager(new JsonAddressBookStorage(storage.getAddressBookFilePath()),
                new JsonUserPrefsStorage(temporaryFolder.resolve("otherPreferences.json")));
        Model staleModel = new ModelManager(staleStorage.readAddressBook().orElseThrow(), new UserPrefs());
        AddressBook originalRecords = new AddressBook(staleModel.getAddressBook());
        Logic staleLogic = new LogicManager(staleModel, staleStorage);
        staleLogic.execute("find n/Benson");

        logic.execute("add n/Samuel i/A0123456B e/sam@example.com");
        String latestContents = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("add n/Carla i/C0123456D e/carla@example.com", "edit i/A1234567B n/Updated",
                "delete i/B1234567C", "clear", "mark i/A1234567B d/2026-10-21 l/Tutorial 1",
                "unmark i/A1234567B d/2026-10-20 l/Tutorial 1")) {
            assertThrows(CommandException.class, LogicManager.MESSAGE_DATA_CHANGED, () -> staleLogic.execute(command));
            assertEquals(latestContents, Files.readString(storage.getAddressBookFilePath()));
            assertEquals(originalRecords, staleModel.getAddressBook());
            assertEquals(List.of(BENSON), staleModel.getFilteredPersonList());
        }
        assertEquals(UndoCommand.MESSAGE_NOTHING_TO_UNDO, staleLogic.execute("undo").getFeedbackToUser());
    }

    @Test
    public void execute_undoAfterExternalSave_preservesFileAndUndoHistory() throws Exception {
        model.addPerson(ALICE);
        logic.execute("add n/Samuel i/A0123456B e/sam@example.com");
        logic.execute("find n/Samuel");
        AddressBook originalRecords = new AddressBook(model.getAddressBook());
        Person samuel = model.getFilteredPersonList().getFirst();
        Path file = storage.getAddressBookFilePath();
        String previousContents = Files.readString(file);
        JsonAddressBookStorage otherStorage = new JsonAddressBookStorage(file);
        AddressBook newerRecords = new AddressBook(otherStorage.readAddressBook().orElseThrow());
        newerRecords.addPerson(BENSON);
        otherStorage.saveAddressBook(newerRecords);
        String latestContents = Files.readString(file);

        assertThrows(CommandException.class, LogicManager.MESSAGE_DATA_CHANGED, () -> logic.execute("undo"));
        assertEquals(latestContents, Files.readString(file));
        assertEquals(originalRecords, model.getAddressBook());
        assertEquals(List.of(samuel), model.getFilteredPersonList());

        // Restoring the exact expected file allows a retry and demonstrates that failed undo kept its snapshot.
        Files.writeString(file, previousContents);
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_fileLocked_reportsRetryAndPreservesModel() throws Exception {
        JsonAddressBookStorage busyStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new DataFileInUseException();
            }
        };
        logic = new LogicManager(model, new StorageManager(busyStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        assertThrows(CommandException.class, LogicManager.MESSAGE_DATA_IN_USE, () ->
                logic.execute("add n/Samuel i/A0123456B e/sam@example.com"));
        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(UndoCommand.MESSAGE_NOTHING_TO_UNDO, logic.execute("undo").getFeedbackToUser());
    }
    // @@author

    @Test
    public void execute_unmark_persistsRemovalAndPreservesFilter() throws Exception {
        // EP: successful unmark of a hidden student persists only the matching removal and keeps the active filter
        AttendanceRecord record = new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        AttendanceRecord retained = new AttendanceRecord(LocalDate.parse("2026-10-21"), new Label("Tutorial 1"));
        Person student = new PersonBuilder(ALICE).withLabels().withAttendance(record, retained).build();
        model.addPerson(student);
        model.addPerson(BENSON);
        logic.execute("find n/Benson");

        CommandResult result = logic.execute("unmark i/a1234567b d/2026-10-20 l/tutorial 1");

        assertEquals("Unmarked student A1234567B for tutorial 1 on 2026-10-20.", result.getFeedbackToUser());
        Person expectedStudent = new PersonBuilder(student).withAttendance(retained).build();
        assertEquals(List.of(expectedStudent, BENSON), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_filterPreservingCommands_keepSameFilterPredicate() throws Exception {
        // EP: repeated mark/unmark/delete under a find filter must not wrap the predicate in extra layers (#206)
        model.addPerson(new PersonBuilder(ALICE).withLabels("Tutorial 1").build());
        model.addPerson(BENSON);
        model.addPerson(CARL);
        logic.execute("find n/Benson");
        Predicate<? super Person> findPredicate = model.getFilteredPersonListPredicate();

        for (int i = 0; i < 5; i++) {
            logic.execute("mark i/A1234567B d/2026-10-20 l/Tutorial 1");
            logic.execute("unmark i/A1234567B d/2026-10-20 l/Tutorial 1");
        }
        logic.execute("delete i/" + CARL.getStudentId());

        assertSame(findPredicate, model.getFilteredPersonListPredicate());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_markThenUnmark_restoresAndReloadsOriginalAttendance() throws Exception {
        Person student = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        model.addPerson(student);

        logic.execute("mark i/A1234567B d/2026-10-20 l/Tutorial 1");
        logic.execute("unmark i/A1234567B d/2026-10-20 l/Tutorial 1");

        assertEquals(List.of(student), model.getAddressBook().getPersonList());
        assertEquals(List.of(student), storage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_undo_restoresLatestSnapshotSavesAndShowsAllStudents() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        logic.execute("add n/Samuel i/A0123456B e/samuel@example.com");
        logic.execute("find n/Samuel");

        CommandResult result = logic.execute("undo");

        assertEquals(UndoCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(originalAddressBook, storage.readAddressBook().orElseThrow());
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_undoWithoutHistory_reportsNothingToUndo() throws Exception {
        model.addPerson(ALICE);
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        CommandResult result = logic.execute("undo");

        assertEquals(UndoCommand.MESSAGE_NOTHING_TO_UNDO, result.getFeedbackToUser());
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_undoAfterUnchangedCommands_skipsThemAndRevertsLastRealChange() throws Exception {
        // EP: commands that leave the records unchanged add no undo snapshot (#207)
        model.addPerson(ALICE);
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        logic.execute("add n/Samuel i/A0123456B e/samuel@example.com");
        logic.execute("edit i/A0123456B n/Samuel");

        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(UndoCommand.MESSAGE_NOTHING_TO_UNDO, logic.execute("undo").getFeedbackToUser());
    }

    @Test
    public void execute_undoAfterClearingEmptyList_reportsNothingToUndo() throws Exception {
        // Boundary: clear on an empty list succeeds but has nothing to reverse
        logic.execute("clear");

        assertEquals(UndoCommand.MESSAGE_NOTHING_TO_UNDO, logic.execute("undo").getFeedbackToUser());
    }

    @Test
    public void execute_undoSaveFailure_preservesDataFileViewAndHistory() throws Exception {
        ToggleableJsonAddressBookStorage addressBookStorage =
                new ToggleableJsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage);
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        logic.execute("add n/Samuel i/A0123456B e/samuel@example.com");
        Person addedStudent = model.getAddressBook().getPersonList().getLast();
        logic.execute("find n/Samuel");
        String originalFile = Files.readString(storage.getAddressBookFilePath());
        addressBookStorage.setFailWrites(true);

        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute("undo"));
        assertEquals(List.of(ALICE, BENSON, addedStudent), model.getAddressBook().getPersonList());
        assertEquals(List.of(addedStudent), model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));

        addressBookStorage.setFailWrites(false);
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(originalAddressBook, model.getAddressBook());
    }

    @Test
    public void execute_consecutiveUndo_discardsEachReversedState() throws Exception {
        logic.execute("add n/First Student i/A0000001B e/first@example.com");
        Person firstStudent = model.getAddressBook().getPersonList().getFirst();
        logic.execute("add n/Second Student i/A0000002B e/second@example.com");
        Person secondStudent = model.getAddressBook().getPersonList().getLast();
        logic.execute("add n/Third Student i/A0000003B e/third@example.com");

        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(firstStudent, secondStudent), model.getAddressBook().getPersonList());
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(firstStudent), model.getAddressBook().getPersonList());
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(), model.getAddressBook().getPersonList());
        assertEquals(UndoCommand.MESSAGE_NOTHING_TO_UNDO, logic.execute("undo").getFeedbackToUser());
    }

    @Test
    public void execute_changeAfterUndo_createsOnlyANewPredecessor() throws Exception {
        logic.execute("add n/First Student i/A0000001B e/first@example.com");
        Person firstStudent = model.getAddressBook().getPersonList().getFirst();
        logic.execute("add n/Second Student i/A0000002B e/second@example.com");
        Person secondStudent = model.getAddressBook().getPersonList().getLast();
        logic.execute("add n/Third Student i/A0000003B e/third@example.com");

        logic.execute("undo");
        logic.execute("add n/Fourth Student i/A0000004B e/fourth@example.com");
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(firstStudent, secondStudent), model.getAddressBook().getPersonList());
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(firstStudent), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_undoHistoryRetainsOnlyOneHundredPriorStates() throws Exception {
        int historyLimit = LogicManager.MAX_UNDO_HISTORY_SIZE;
        int numberOfChanges = historyLimit + 1;
        for (int changeNumber = 1; changeNumber <= numberOfChanges; changeNumber++) {
            logic.execute(String.format("add n/Student i/A%07dB e/student%d@example.com",
                    changeNumber, changeNumber));
        }

        for (int undoCount = 0; undoCount < historyLimit; undoCount++) {
            assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute("undo").getFeedbackToUser());
        }
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(UndoCommand.MESSAGE_NOTHING_TO_UNDO, logic.execute("undo").getFeedbackToUser());
    }

    @Test
    public void execute_unmarkFailure_preservesSavedDataAndFilter() throws Exception {
        // EP: parse and command failures preserve the live model, active filter, and saved file
        AttendanceRecord record = new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        Person student = new PersonBuilder(ALICE).withAttendance(record).build();
        model.addPerson(student);
        logic.execute("find n/Benson");
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_DATE, () ->
                logic.execute("unmark i/A1234567B d/2026-02-30 l/Tutorial 1"));
        String expectedMessage = "Student A1234567B is not marked present for tutorial 1 on 2026-10-21.";
        assertThrows(CommandException.class, expectedMessage, () ->
                logic.execute("unmark i/A1234567B d/2026-10-21 l/Tutorial 1"));
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(List.of(student), model.getAddressBook().getPersonList());
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_unmarkSaveFailure_keepsLiveModelFileAndFilter() throws Exception {
        // EP: persistence failure after a valid unmark leaves the live model, filter, and saved file unchanged
        AttendanceRecord record = new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        Person student = new PersonBuilder(ALICE).withAttendance(record).build();
        model.addPerson(student);
        model.addPerson(BENSON);
        logic.execute("find n/Benson");
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
                throw new IOException("Simulated replacement failure");
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        failingStorage.readAddressBook();
        assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () ->
                logic.execute("unmark i/A1234567B d/2026-10-20 l/Tutorial 1"));
        assertEquals(List.of(student, BENSON), model.getAddressBook().getPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteHiddenStudent_preservesFilterAndPersists() throws Exception {
        // EP: delete a student hidden by the active filter
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        logic.execute("find n/Benson");
        CommandResult result = logic.execute("delete i/a1234567b");
        assertEquals("Deleted student A1234567B: Alice Pauline.", result.getFeedbackToUser());
        assertEquals(List.of(BENSON), model.getAddressBook().getPersonList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_mark_persistsAndReloadsAttendance() throws Exception {
        // EP: successful mark of a hidden student persists the record and preserves the active filter
        Person student = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        model.addPerson(student);
        logic.execute("find n/Benson");

        CommandResult result = logic.execute("mark i/a1234567b d/2026-10-20 l/tutorial 1");

        assertEquals("Marked student A1234567B present for tutorial 1 on 2026-10-20.",
                result.getFeedbackToUser());
        AttendanceRecord record = new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        Person expectedStudent = new PersonBuilder(student).withAttendance(record).build();
        assertEquals(List.of(expectedStudent), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_attendance_showsReportAndRefreshesAfterMark() throws Exception {
        // EP: open attendance report refreshes after a matching mark, not an unrelated date; other commands close it
        Person alice = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        Person benson = new PersonBuilder(BENSON).withLabels("Tutorial 1").build();
        model.addPerson(alice);
        model.addPerson(benson);
        LocalDate date = LocalDate.parse("2026-10-20");
        Label group = new Label("tutorial 1");

        CommandResult shown = logic.execute("attendance d/2026-10-20 l/tutorial 1");
        assertEquals("Attendance for tutorial 1 on 2026-10-20: 0 present, 2 absent.", shown.getFeedbackToUser());
        assertEquals(AttendanceReport.of(List.of(alice, benson), date, group),
                shown.getAttendanceReport().orElseThrow());

        CommandResult marked = logic.execute("mark i/A1234567B d/2026-10-20 l/Tutorial 1");
        AttendanceReport refreshed = marked.getAttendanceReport().orElseThrow();
        assertEquals(AttendanceReport.of(model.getAddressBook().getPersonList(), date, group), refreshed);
        assertEquals(1, refreshed.getPresentCount());
        assertEquals(1, refreshed.getAbsentCount());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        AttendanceReport afterOtherDate = logic.execute("mark i/B1234567C d/2026-10-21 l/Tutorial 1")
                .getAttendanceReport().orElseThrow();
        assertEquals(date, afterOtherDate.getDate());
        assertEquals(1, afterOtherDate.getPresentCount());
        assertEquals(1, afterOtherDate.getAbsentCount());

        assertCommandException("mark i/A1234567B d/2026-10-20 l/Tutorial 1",
                "Student A1234567B is already marked present for tutorial 1 on 2026-10-20.");
        assertEquals(2, logic.execute("mark i/B1234567C d/2026-10-20 l/Tutorial 1")
                .getAttendanceReport().orElseThrow().getPresentCount());

        assertEquals(Optional.empty(), logic.execute("list").getAttendanceReport());
        assertEquals(Optional.empty(), logic.execute("mark i/A1234567B d/2026-10-22 l/Tutorial 1")
                .getAttendanceReport());
    }

    @Test
    public void execute_unmarkAndUndo_updateAttendanceView() throws Exception {
        // EP: open attendance report refreshes after unmark; undo closes the report
        model.addPerson(new PersonBuilder(ALICE).withLabels("Tutorial 1").build());
        logic.execute("mark i/A1234567B d/2026-10-20 l/Tutorial 1");
        assertEquals(1, logic.execute("attendance d/2026-10-20 l/Tutorial 1")
                .getAttendanceReport().orElseThrow().getPresentCount());

        CommandResult unmarked = logic.execute("unmark i/A1234567B d/2026-10-20 l/Tutorial 1");
        assertEquals(0, unmarked.getAttendanceReport().orElseThrow().getPresentCount());

        assertEquals(Optional.empty(), logic.execute("undo").getAttendanceReport());
    }

    @Test
    public void execute_attendanceAfterRestart_showsSavedAttendance() throws Exception {
        // EP: persisted records survive restart and reports distinguish requested groups and no-member groups
        model.addPerson(new PersonBuilder(ALICE).withLabels("Tutorial 1").build());
        model.addPerson(new PersonBuilder(BENSON).withLabels("Tutorial 1", "Tutorial 2").build());
        logic.execute("mark i/A1234567B d/2026-10-20 l/Tutorial 1");
        logic.execute("mark i/B1234567C d/2026-10-20 l/Tutorial 2");

        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);

        assertEquals("Attendance for tutorial 1 on 2026-10-20: 1 present, 1 absent.",
                logic.execute("attendance d/2026-10-20 l/Tutorial 1").getFeedbackToUser());
        assertEquals("Attendance for tutorial 2 on 2026-10-20: 1 present, 0 absent.",
                logic.execute("attendance d/2026-10-20 l/Tutorial 2").getFeedbackToUser());
        assertEquals("Attendance for tutorial 3 on 2026-10-20: 0 present, 0 absent.",
                logic.execute("attendance d/2026-10-20 l/Tutorial 3").getFeedbackToUser());
    }

    @Test
    public void execute_markFailure_preservesSavedData() throws Exception {
        // EP: parse and command failures preserve the live model and saved file
        Person student = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        model.addPerson(student);
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        assertParseException("mark i/A1234567B d/2026-02-30 l/Tutorial 1", ParserUtil.MESSAGE_INVALID_DATE);
        assertCommandException("mark i/A1234567B d/2026-10-20 l/Tutorial 2",
                "Student A1234567B does not have group label \"tutorial 2\".");
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(List.of(student), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_markSaveFailure_keepsLiveModelAndFile() throws Exception {
        // EP: persistence failure after a valid mark leaves the live model and saved file unchanged
        Person student = new PersonBuilder(ALICE).withLabels("Tutorial 1").build();
        model.addPerson(student);
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
                throw new IOException("Simulated replacement failure");
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        failingStorage.readAddressBook();
        assertCommandException("mark i/A1234567B d/2026-10-20 l/Tutorial 1", LogicManager.MESSAGE_SAVE_FAILURE);
        assertEquals(List.of(student), model.getAddressBook().getPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_delete_removesEntireStudentRecordFromSavedData() throws Exception {
        // EP: delete an existing student and persist the removal
        Person student = new PersonBuilder().withStudentId("A0123456B").withName("Alex Tan")
                .withEmail("alex@example.com").withRemark("Weak in recursion.").build();
        model.addPerson(student);
        storage.saveAddressBook(model.getAddressBook());
        assertEquals("Deleted student A0123456B: Alex Tan.",
                logic.execute("delete i/A0123456B").getFeedbackToUser());
        assertEquals(List.of(), model.getAddressBook().getPersonList());
        assertEquals(List.of(), storage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_deleteInvalidOrUnknownId_preservesRecordsAndSavedFile() throws Exception {
        // EP: invalid or unknown delete ID leaves data unchanged
        model.addPerson(ALICE);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());
        assertParseException("delete i/", "Parameter i/ cannot be empty.");
        assertParseException("delete i/A1234567B x/value", "Unknown parameter: x/.");
        assertCommandException("delete i/Z9999999Z", "Student with ID Z9999999Z is not in the records.");
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
    }

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        // EP: null model
        assertThrows(NullPointerException.class, () -> new LogicManager(null, storage));
        // EP: null storage
        assertThrows(NullPointerException.class, () -> new LogicManager(model, null));
        // EP: null startup message
        assertThrows(NullPointerException.class, () -> new LogicManager(model, storage, true, null));
    }

    @Test
    public void execute_nullCommandText_throwsNullPointerException() {
        // EP: null commandText
        assertThrows(NullPointerException.class, () -> logic.execute(null));
    }

    @Test
    public void execute_add_successSavesBeforePublishing() throws Exception {
        // EP: add a valid student and persist before publishing
        CommandResult result = logic.execute("add n/Samuel i/a0123456b e/Sam@EXAMPLE.COM r/Quiz: 8/10.");
        assertEquals("Added student A0123456B: Samuel. Remark: Quiz: 8/10.\n1 student listed.",
                result.getFeedbackToUser());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals("Sam@example.com", model.getAddressBook().getPersonList().getFirst().getEmail().value);
    }

    @Test
    public void execute_addWithLabel_persistsAndFindsAfterReload() throws Exception {
        // EP: add a student with a label and reload
        model.addPerson(ALICE);
        // @@author ananyakharbanda
        logic.execute("add n/Samuel i/A0101010A e/samuel@example.com l/Tutorial 1");
        Person expectedStudent = new PersonBuilder().withName("Samuel")
                .withStudentId("A0101010A").withEmail("samuel@example.com").withLabels("Tutorial 1").build();
        // @@author
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);

        // @@author ananyakharbanda
        CommandResult result = logic.execute("find l/tutorial 1");
        assertEquals("1 student listed.", result.getFeedbackToUser());
        assertEquals(List.of(expectedStudent), model.getFilteredPersonList());
        // @@author
    }

    @Test
    public void execute_editUnicodeEquivalentLabel_keepsSavedDataReadable() throws Exception {
        // EP: edit with a Unicode-equivalent label fails without corrupting saved data
        String studentId = "A0123456B";
        String storedLabel = "\u0130stanbul";
        String equivalentLabel = "i\u0307stanbul";
        logic.execute("add n/Alex i/" + studentId + " e/alex@example.com l/" + storedLabel);
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        assertCommandException("edit i/" + studentId + " l/" + equivalentLabel,
                String.format(EditCommand.MESSAGE_DUPLICATE_LABEL, studentId, equivalentLabel));
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        logic.execute("edit i/" + studentId + " dl/" + equivalentLabel);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(0, model.getAddressBook().getPersonList().getFirst().getLabels().size());
    }

    @Test
    public void execute_readOnlyCommands_doNotSave() throws Exception {
        model.addPerson(new PersonBuilder().withName("Samuel Tan")
                .withStudentId("A0101010A")
                .withEmail("samuel@example.com")
                .withLabels("Discrete Math Tutorial")
                .build());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("unused.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Read-only commands must not save");
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("list");
        logic.execute("find n/ Samuel");
        logic.execute("find l/Discrete Math Tutorial");
        logic.execute("attendance d/2026-10-20 l/Discrete Math Tutorial");
        logic.execute("help");
        logic.execute("exit");
    }

    @Test
    public void execute_loadingFailed_blocksDataCommandsAndPreservesCorruptFile() throws Exception {
        // EP: data load failure blocks modifying commands and student-list queries
        Files.writeString(storage.getAddressBookFilePath(), "invalid student data");
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        logic = new LogicManager(model, storage, false, "");
        for (String command : List.of("add n/Samuel i/A0123456B e/sam@example.com", "clear", "delete i/B1234567C",
                "edit i/A0123456B n/Samuel", "unmark i/A0123456B d/2026-10-20 l/Tutorial 1",
                "list", "find n/Alice", "find i/A1234567B", "find e/alice@example.com", "find r/recursion",
                "find l/Tutorial 1", "attendance d/2026-10-20 l/Tutorial 1")) {
            assertCommandException(command, LogicManager.MESSAGE_DATA_UNAVAILABLE);
        }

        // EP: invalid query syntax is still reported before the loading-failure guard
        assertParseException("find p/Alice", "Unknown parameter: p/.");
        // @@author ananyakharbanda
        assertParseException("attendance d/2026-02-30 l/Tutorial 1", ParserUtil.MESSAGE_INVALID_DATE);

        // EP: help and exit remain available without changing the corrupt file
        // @@author
        logic.execute("help");
        logic.execute("exit");
        assertEquals("invalid student data", Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_loadedEmptyAddressBook_reportsGenuineEmptyListAndFindResults() throws Exception {
        // EP: successfully loaded empty data reports empty results, rather than a loading failure
        // BVA: zero stored students
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        assertEquals(ListCommand.MESSAGE_NO_STUDENTS_FOUND, logic.execute("list").getFeedbackToUser());
        // @@author ananyakharbanda
        assertEquals(0, logic.execute("attendance d/2026-10-20 l/Tutorial 1")
                .getAttendanceReport().orElseThrow().getEntries().size());
        // @@author
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0),
                logic.execute("find n/Alice").getFeedbackToUser());
        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_replacementFails_preservesRecordsFileAndActiveFilter() throws Exception {
        // EP: failed save preserves student records and filter
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        storage.saveAddressBook(model.getAddressBook());
        String previousFile = Files.readString(storage.getAddressBookFilePath());
        AddressBook previousRecords = new AddressBook(model.getAddressBook());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
                assertEquals(previousRecords, model.getAddressBook());
                throw new IOException("Simulated replacement failure");
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        failingStorage.readAddressBook();
        for (String command : List.of("add n/Samuel i/A0123456B e/sam@example.com", "delete i/B1234567C", "clear",
                "edit i/A1234567B n/Updated Name", "edit i/A1234567B l/Tutorial A")) {
            assertThrows(CommandException.class, LogicManager.MESSAGE_SAVE_FAILURE, () -> logic.execute(command));
            assertEquals(previousRecords, model.getAddressBook());
            assertEquals(List.of(BENSON), model.getFilteredPersonList());
            assertEquals(previousFile, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_storageFolderCannotBeCreated_preservesRecords() throws Exception {
        // EP: unwritable storage folder preserves student records
        Path blockingFile = Files.writeString(temporaryFolder.resolve("data"), "not a folder");
        logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(blockingFile.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        assertCommandException("add n/Samuel i/A0123456B e/sam@example.com",
                LogicManager.MESSAGE_STORAGE_LOCATION_FAILURE);
        assertEquals(new AddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_deleteAfterFind_usesStudentIdAndPersists() throws Exception {
        // EP: delete by Student ID after filtering
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        logic.execute("find n/Benson");
        logic.execute("delete i/B1234567C");
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("list");
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        // EP: malformed command text
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        // EP: well-formed command with execution error
        String deleteCommand = "delete i/Z9999999Z";
        assertCommandException(deleteCommand,
                "Student with ID Z9999999Z is not in the records.");
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        // EP: valid command executes successfully
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_NO_STUDENTS_FOUND, model);
    }

    @Test
    // @@author ananyakharbanda
    public void execute_removedLabelCommand_preservesRecordsAndSavedFile() throws Exception {
        model.addPerson(ALICE);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Nobody")));
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, ()
            -> logic.execute("label l/Tutorial 1 i/" + ALICE.getStudentId()));
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
        // @@author
    }

    @Test
    public void execute_findLabelExactCase_success() throws Exception {
        Person samuel = new PersonBuilder().withName("Samuel Tan")
                .withStudentId("A0101010A")
                .withEmail("samuel@example.com")
                .withLabels("Discrete Math Tutorial")
                .build();
        Person alex = new PersonBuilder().withName("Alex Yeoh")
                .withStudentId("A0101011A")
                .withEmail("alex@example.com")
                .withLabels("CS2103T Tutorial")
                .build();
        model.addPerson(samuel);
        model.addPerson(alex);
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> person.equals(samuel));

        // @@author ananyakharbanda
        assertCommandSuccess("find l/Discrete Math Tutorial",
                "1 student listed.", expectedModel);
        // @@author
        assertEquals(List.of(samuel), model.getFilteredPersonList());
        assertEquals(originalAddressBook, model.getAddressBook());
    }

    @Test
    public void execute_findLabelDifferentCase_success() throws Exception {
        Person samuel = new PersonBuilder().withName("Samuel Tan")
                .withStudentId("A0101010A")
                .withEmail("samuel@example.com")
                .withLabels("Discrete Math Tutorial")
                .build();
        Person alex = new PersonBuilder().withName("Alex Yeoh")
                .withStudentId("A0101011A")
                .withEmail("alex@example.com")
                .withLabels("CS2103T Tutorial")
                .build();
        model.addPerson(samuel);
        model.addPerson(alex);
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> person.equals(samuel));

        // @@author ananyakharbanda
        assertCommandSuccess("find l/discrete math tutorial",
                "1 student listed.", expectedModel);
        // @@author
        assertEquals(List.of(samuel), model.getFilteredPersonList());
        assertEquals(originalAddressBook, model.getAddressBook());
    }

    @Test
    // @@author ananyakharbanda
    public void execute_findUnicodeLabels_agreesWithStorageAndEdit() throws Exception {
        Person latinI = new PersonBuilder().withStudentId("A0101010A").withLabels("I").build();
        Person dotlessI = new PersonBuilder().withStudentId("A0101011A").withLabels("ı").build();
        Person dottedI = new PersonBuilder().withStudentId("A0101012A").withLabels("İ").build();
        model.addPerson(latinI);
        model.addPerson(dotlessI);
        model.addPerson(dottedI);
        storage.saveAddressBook(model.getAddressBook());
        model.setAddressBook(storage.readAddressBook().orElseThrow());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        logic.execute("find l/i");
        assertEquals(List.of(latinI), model.getFilteredPersonList());
        logic.execute("find l/ı");
        assertEquals(List.of(dotlessI), model.getFilteredPersonList());
        logic.execute("find l/i\u0307");
        assertEquals(List.of(dottedI), model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));

        logic.execute("edit i/A0101010A dl/i");
        logic.execute("find l/i");
        assertEquals(List.of(), model.getFilteredPersonList());
        logic.execute("find l/ı");
        assertEquals(List.of(dotlessI), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_findRemarksAndLabelsSequentially_preservesBothSearchModesAndSavedData() throws Exception {
        Person alex = new PersonBuilder().withStudentId("A0101010A").withLabels("Tutorial 1")
                .withRemark("Needs help with recursion").build();
        Person samuel = new PersonBuilder().withStudentId("A0101011A").withLabels("Tutorial 2")
                .withRemark("Practise loops").build();
        model.addPerson(alex);
        model.addPerson(samuel);
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        logic.execute("find l/tutorial 1");
        assertEquals(List.of(alex), model.getFilteredPersonList());
        logic.execute("find r/LOOPS");
        assertEquals(List.of(samuel), model.getFilteredPersonList());
        logic.execute("find l/tutorial 1");
        assertEquals(List.of(alex), model.getFilteredPersonList());
        logic.execute("find r/curs");
        assertEquals(List.of(alex), model.getFilteredPersonList());
        logic.execute("find r/NoSuchRemark");
        assertEquals(List.of(), model.getFilteredPersonList());
        logic.execute("find l/tutorial 2");
        assertEquals(List.of(samuel), model.getFilteredPersonList());

        assertThrows(ParseException.class, "Parameter n/, i/, e/, r/, or l/ must be specified only once.", ()
            -> logic.execute("find l/Tutorial 1 r/loops"));
        assertEquals(List.of(samuel), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(alex, samuel), model.getFilteredPersonList());
        assertEquals(List.of(alex, samuel), model.getAddressBook().getPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
    }
    // @@author

    @Test
    public void execute_findLabelNoMatches_clearsDisplayedList() throws Exception {
        Person samuel = new PersonBuilder().withName("Samuel Tan")
                .withStudentId("A0101010A")
                .withEmail("samuel@example.com")
                .withLabels("Discrete Math Tutorial")
                .build();
        Person alex = new PersonBuilder().withName("Alex Yeoh")
                .withStudentId("A0101011A")
                .withEmail("alex@example.com")
                .withLabels("CS2103T Tutorial")
                .build();
        model.addPerson(samuel);
        model.addPerson(alex);
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> false);

        // @@author ananyakharbanda
        assertCommandSuccess("find l/NonExistent",
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0), expectedModel);
        // @@author
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(originalAddressBook, model.getAddressBook());
    }

    // @@author ananyakharbanda
    @Test
    public void execute_findLabelAfterOtherSearches_searchesAllStudentsAndListRestoresAll() throws Exception {
        Person labelledAlice = new PersonBuilder(ALICE).withLabels("Tutorial 1", "Consultation").build();
        model.addPerson(labelledAlice);
        model.addPerson(BENSON);
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        logic.execute("find n/Benson");
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        logic.execute("find l/tutorial 1");
        assertEquals(List.of(labelledAlice), model.getFilteredPersonList());
        logic.execute("find e/" + BENSON.getEmail().value);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        logic.execute("find i/" + labelledAlice.getStudentId());
        assertEquals(List.of(labelledAlice), model.getFilteredPersonList());
        logic.execute("find l/No Such Label");
        assertEquals(List.of(), model.getFilteredPersonList());
        logic.execute("find l/Consultation");
        assertEquals(List.of(labelledAlice), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(labelledAlice, BENSON), model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_invalidLabelSearch_preservesActiveViewAndSavedFile() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        logic.execute("find n/Benson");
        storage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(storage.getAddressBookFilePath());

        for (String command : List.of("find l/", "find l/One l/Two", "find n/Alice l/Tutorial",
                "find l/Tutorial/1", "filter l/Tutorial")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(BENSON), model.getFilteredPersonList());
            assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
            assertEquals(originalFile, Files.readString(storage.getAddressBookFilePath()));
        }
        // @@author
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, LogicManager.MESSAGE_SAVE_FAILURE);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, LogicManager.MESSAGE_SAVE_FAILURE);
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + STUDENT_ID_DESC_AMY
                + EMAIL_DESC_AMY;
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    /**
     * A JSON storage implementation whose replacement failures can be enabled after an initial successful save.
     */
    private static class ToggleableJsonAddressBookStorage extends JsonAddressBookStorage {
        private boolean failWrites;

        ToggleableJsonAddressBookStorage(Path filePath) {
            super(filePath);
        }

        void setFailWrites(boolean failWrites) {
            this.failWrites = failWrites;
        }

        @Override
        protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
            if (failWrites) {
                throw new IOException("Simulated replacement failure");
            }
            super.replaceDataFile(temporaryFile, targetFile);
        }
    }
}
