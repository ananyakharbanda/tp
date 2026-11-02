package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        // EP: nonempty unfiltered address book.
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        // EP: partially filtered address book restores all records without changing data.
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_showsNoStudentsFound() {
        // EP: empty address book has distinct feedback.
        // BVA: zero stored students.
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        Model expectedEmptyModel = new ModelManager(new AddressBook(), new UserPrefs());

        assertCommandSuccess(
                new ListCommand(),
                emptyModel,
                ListCommand.MESSAGE_NO_STUDENTS_FOUND,
                expectedEmptyModel);
    }

    @Test
    public void execute_noVisibleMatchesButOneStoredStudent_showsStudent() {
        // EP: an empty filtered view differs from an empty address book.
        // BVA: one stored student, immediately above the empty-data boundary.
        Model singleModel = new ModelManager();
        singleModel.addPerson(ALICE);
        Model expected = new ModelManager(singleModel.getAddressBook(), new UserPrefs());
        singleModel.updateFilteredPersonList(person -> false);
        assertCommandSuccess(new ListCommand(), singleModel, ListCommand.MESSAGE_SUCCESS, expected);
    }

    @Test
    public void execute_nullModel_rejected() {
        // EP: null violates the command's required model contract.
        ListCommand command = new ListCommand();
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
