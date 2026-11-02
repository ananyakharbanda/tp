package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.FindCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        NameContainsKeywordsPredicate firstPredicate =
                new NameContainsKeywordsPredicate(List.of("first"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_nonMatchingKeyword_noPersonFound() {
        // EP: valid search with no matches in a populated address book.
        // BVA: zero results, immediately below the singular-result boundary.
        String expectedMessage = "0 students listed.";
        NameContainsKeywordsPredicate predicate = preparePredicate("Nobody");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        // EP: multiple keywords use OR matching and retain address-book order.
        String expectedMessage = "3 students listed.";
        NameContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_remarkKeywords_findsMatchesAcrossCompleteList() throws ParseException {
        // EP: filtered views do not restrict a search; empty remarks do not match.
        // BVA: two results is the first plural-result count.
        Person recursionStudent = new PersonBuilder(CARL).withRemark("Needs help with Recursion").build();
        Person loopsStudent = new PersonBuilder(ELLE).withRemark("Practising loops").build();
        Person noRemarkStudent = new PersonBuilder(FIONA).withRemark("").build();
        for (Model currentModel : List.of(model, expectedModel)) {
            currentModel.setPerson(CARL, recursionStudent);
            currentModel.setPerson(ELLE, loopsStudent);
            currentModel.setPerson(FIONA, noRemarkStudent);
        }
        model.updateFilteredPersonList(person -> person.equals(noRemarkStudent));

        FindCommand command = new FindCommandParser().parse(" r/CURS loops");
        expectedModel.updateFilteredPersonList(person ->
                person.equals(recursionStudent) || person.equals(loopsStudent));

        assertCommandSuccess(command, model, "2 students listed.", expectedModel);
        assertEquals(List.of(recursionStudent, loopsStudent), model.getFilteredPersonList());
    }

    @Test
    public void execute_singleMatch_usesSingularStudent() {
        // EP: exactly one result uses singular feedback.
        // BVA: one result, between zero and plural results.
        NameContainsKeywordsPredicate predicate = preparePredicate("Elle");
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(new FindCommand(predicate), model, "1 student listed.", expectedModel);
        assertEquals(List.of(ELLE), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAddressBook_noMatches() throws ParseException {
        // EP: valid search against an empty data set.
        // BVA: zero stored students.
        assertCommandSuccess(new FindCommandParser().parse("n/A"), new ModelManager(),
                "0 students listed.", new ModelManager());
    }

    @Test
    public void execute_eachKeywordField_matchesPartialKeywordsIgnoringCase() throws ParseException {
        // EP: each keyword field supports case-insensitive partial matches and OR semantics.
        Person target = new PersonBuilder(CARL).withName("Unique Name").withStudentId("A7654321B")
                .withEmail("unique@example.com").withRemark("Unique remark").build();
        Person other = new PersonBuilder(ELLE).withRemark("").build();
        for (String input : List.of("n/NIQUE absent", "i/7654321 absent", "e/NIQUE@ absent", "r/NIQUE absent")) {
            Model currentModel = new ModelManager();
            currentModel.addPerson(target);
            currentModel.addPerson(other);
            AddressBook original = new AddressBook(currentModel.getAddressBook());
            Model expected = new ModelManager(original, new UserPrefs());
            expected.updateFilteredPersonList(person -> person.equals(target));
            assertCommandSuccess(new FindCommandParser().parse(input), currentModel, "1 student listed.", expected);
            assertEquals(original, currentModel.getAddressBook());
        }
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code NameContainsKeywordsPredicate}.
     */
    private NameContainsKeywordsPredicate preparePredicate(String userInput) {
        return new NameContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }

    @Test
    public void execute_nullModel_rejected() {
        // EP: null violates the command's required model contract.
        FindCommand command = new FindCommand(person -> true);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
