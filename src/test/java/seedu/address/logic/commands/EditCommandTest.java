package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.attendance.AttendanceRecord;
import seedu.address.model.label.Label;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentId;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private static final String VALID_LABEL_TUTORIAL = "Discrete Math Tutorial";

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_otherFieldsEdited_preservesRemark() {
        // EP: edit email while preserving an existing remark
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithRemark = new PersonBuilder(originalPerson)
                .withRemark("Needs help with recursion").build();
        model.setPerson(originalPerson, personWithRemark);
        Person editedPerson = new PersonBuilder(personWithRemark).withEmail(VALID_EMAIL_BOB).build();
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(),
                new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithRemark, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(personWithRemark.getRemark(), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void execute_otherFieldsEdited_preservesLabels() {
        // EP: edit email while preserving existing labels
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithLabel = new PersonBuilder(originalPerson)
                .withLabels(VALID_LABEL_TUTORIAL).build();
        model.setPerson(originalPerson, personWithLabel);
        Person editedPerson = new PersonBuilder(personWithLabel).withEmail(VALID_EMAIL_BOB).build();
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(),
                new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithLabel, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(personWithLabel.getLabels(), model.getFilteredPersonList().get(0).getLabels());
    }

    @Test
    public void execute_otherFieldsEdited_preservesAttendance() {
        // EP: edit email while preserving attendance records
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        AttendanceRecord record = new AttendanceRecord(LocalDate.parse("2026-10-20"), new Label("Tutorial 1"));
        Person student = new PersonBuilder(original).withAttendance(record).build();
        model.setPerson(original, student);
        Person editedStudent = new PersonBuilder(student).withEmail(VALID_EMAIL_BOB).build();
        EditCommand command = new EditCommand(student.getStudentId(),
                new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_BOB).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, editedStudent);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedStudent)), expectedModel);
        assertEquals(student.getAttendance(), model.getFilteredPersonList().get(0).getAttendance());
    }

    @Test
    public void execute_remarkEdited_success() {
        // EP: remark edited
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withRemark(VALID_REMARK_BOB).build();
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(),
                new EditPersonDescriptorBuilder().withRemark(VALID_REMARK_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_emptyRemark_clearsRemark() {
        // EP: empty remark
        // BVA: zero remark characters
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithRemark = new PersonBuilder(originalPerson).withRemark(VALID_REMARK_BOB).build();
        model.setPerson(originalPerson, personWithRemark);
        Person editedPerson = new PersonBuilder(personWithRemark).withRemark("").build();
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(),
                new EditPersonDescriptorBuilder().withRemark("").build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithRemark, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_addLabel_preservesExistingLabels() {
        // EP: add label
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(originalPerson).withLabels("Tutorial A").build();
        model.setPerson(originalPerson, student);
        Person editedStudent = new PersonBuilder(student).withLabels("Tutorial A", "Needs Follow-up").build();
        EditCommand command = new EditCommand(student.getStudentId(),
                new EditPersonDescriptorBuilder().withLabelsToAdd("Needs Follow-up").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, editedStudent);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedStudent)), expectedModel);
    }

    @Test
    public void execute_removeLabel_ignoresCaseAndPreservesOtherLabels() {
        // EP: remove label
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(originalPerson).withLabels("Tutorial A", "Needs Follow-up").build();
        model.setPerson(originalPerson, student);
        Person editedStudent = new PersonBuilder(student).withLabels("Needs Follow-up").build();
        EditCommand command = new EditCommand(student.getStudentId(),
                new EditPersonDescriptorBuilder().withLabelsToRemove("tutorial a").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, editedStudent);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedStudent)), expectedModel);
    }

    @Test
    public void execute_removeAndAddLabel_renamesOneLabel() {
        // EP: remove and add label
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(originalPerson).withLabels("Tutorial A", "Needs Follow-up").build();
        model.setPerson(originalPerson, student);
        Person editedStudent = new PersonBuilder(student).withLabels("Tutorial B", "Needs Follow-up").build();
        EditCommand command = new EditCommand(student.getStudentId(), new EditPersonDescriptorBuilder()
                .withLabelsToRemove("Tutorial A").withLabelsToAdd("Tutorial B").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, editedStudent);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedStudent)), expectedModel);
    }

    @Test
    public void execute_removeAndAddSameLabel_keepsCanonicalName() {
        // EP: removing and readding a label with different capitalization keeps its lowercase name
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(original).withLabels("Tutorial A").build();
        model.setPerson(original, student);
        Person editedStudent = new PersonBuilder(student).withLabels("tutorial a").build();
        EditCommand command = new EditCommand(student.getStudentId(), new EditPersonDescriptorBuilder()
                .withLabelsToRemove("Tutorial A").withLabelsToAdd("tutorial a").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, editedStudent);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedStudent)), expectedModel);
    }

    @Test
    public void execute_clearAlreadyEmptyLabels_success() {
        // EP: clear labels when the student has no labels
        // BVA: zero existing labels
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(original).withLabels().build();
        model.setPerson(original, student);
        EditCommand command = new EditCommand(student.getStudentId(),
                new EditPersonDescriptorBuilder().withClearedLabels().build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(student)), expectedModel);
    }

    @Test
    public void execute_clearLabelsAndRemark_success() {
        // EP: clear labels and remark
        // BVA: zero labels and remark characters after the edit
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(originalPerson)
                .withLabels("Tutorial A", "Needs Follow-up").withRemark("Meet on Monday").build();
        model.setPerson(originalPerson, student);
        Person editedStudent = new PersonBuilder(student).withLabels().withRemark("").build();
        EditCommand command = new EditCommand(student.getStudentId(), new EditPersonDescriptorBuilder()
                .withClearedLabels().withRemark("").build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, editedStudent);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedStudent)), expectedModel);
    }

    @Test
    public void execute_invalidLabelOperation_preservesStudent() {
        // EP: invalid label operations leave the student unchanged
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person student = new PersonBuilder(originalPerson).withLabels("Tutorial A", "Needs Follow-up").build();
        model.setPerson(originalPerson, student);
        EditCommand missingLabel = new EditCommand(student.getStudentId(),
                new EditPersonDescriptorBuilder().withLabelsToRemove("Unknown").build());
        EditCommand duplicateAfterRemoval = new EditCommand(student.getStudentId(), new EditPersonDescriptorBuilder()
                .withLabelsToRemove("Tutorial A").withLabelsToAdd("needs follow-up").build());

        // EP: remove a label the student does not have
        assertCommandFailure(missingLabel, model,
                String.format(EditCommand.MESSAGE_LABEL_NOT_FOUND, student.getStudentId(), "unknown"));
        // EP: add a case-insensitive duplicate after removing a different label
        assertCommandFailure(duplicateAfterRemoval, model,
                String.format(EditCommand.MESSAGE_DUPLICATE_LABEL, student.getStudentId(), "needs follow-up"));
        assertEquals(student, model.getAddressBook().getPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        // EP: all fields specified unfiltered list
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withName(PersonBuilder.DEFAULT_NAME)
                .withEmail(PersonBuilder.DEFAULT_EMAIL).withRemark(PersonBuilder.DEFAULT_REMARK).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(), descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        // EP: some fields specified unfiltered list
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withEmail(VALID_EMAIL_BOB).build();
        EditCommand editCommand = new EditCommand(lastPerson.getStudentId(), descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        // EP: no field specified unfiltered list
        // BVA: zero requested field changes in a direct command
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(), new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        // EP: filtered list
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(),
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameNameAsExistingPersonUnfilteredList_success() {
        // EP: same name as existing person unfiltered list
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(secondPerson).withName(firstPerson.getName().fullName)
                .withEmail(firstPerson.getEmail().value).withRemark(firstPerson.getRemark().value).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(BENSON.getStudentId(), descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(secondPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameNameAsExistingPersonFilteredList_success() {
        // EP: same name as existing person filtered list
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withName(personInList.getName().fullName)
                .withEmail(personInList.getEmail().value).withRemark(personInList.getRemark().value).build();
        EditCommand editCommand = new EditCommand(ALICE.getStudentId(),
                new EditPersonDescriptorBuilder(personInList).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_unknownStudentId_failure() {
        // EP: unknown student id
        StudentId unknownStudentId = new StudentId("Z1234567Z");
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(unknownStudentId, descriptor);

        assertCommandFailure(editCommand, model,
                String.format(EditCommand.MESSAGE_STUDENT_NOT_FOUND, unknownStudentId));
    }

    @Test
    public void execute_studentHiddenByFilter_success() {
        // EP: student hidden by filter
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(BENSON).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(BENSON.getStudentId(),
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(BENSON, editedPerson);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(ALICE.getStudentId(), DESC_AMY);

        // EP: same Student ID and descriptor values
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(ALICE.getStudentId(), copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // EP: same object
        assertTrue(standardCommand.equals(standardCommand));

        // EP: null
        assertFalse(standardCommand.equals(null));

        // EP: different type
        assertFalse(standardCommand.equals(new ClearCommand()));

        // EP: different Student ID
        assertFalse(standardCommand.equals(new EditCommand(BENSON.getStudentId(), DESC_AMY)));

        // EP: different edit descriptor
        assertFalse(standardCommand.equals(new EditCommand(ALICE.getStudentId(), DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        // EP: valid object representation
        StudentId studentId = ALICE.getStudentId();
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(studentId, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{targetStudentId=" + studentId
                + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}

