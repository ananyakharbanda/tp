package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DELETE_LABEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LABEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_ID;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.label.Label;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.model.person.StudentId;

/**
 * Edits the details of an existing student in TeachAssist.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the student identified "
            + "by their Student ID. Add labels with l/ and remove them with dl/. "
            + "An empty l/ clears all labels; an empty r/ clears the remark.\n"
            + "Parameters: " + PREFIX_STUDENT_ID + "STUDENT_ID "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_REMARK + "REMARK] "
            + "[" + PREFIX_LABEL + "LABEL_NAME]... "
            + "[" + PREFIX_DELETE_LABEL + "LABEL_NAME]...\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STUDENT_ID + "A0123456B "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited student: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This student already exists in TeachAssist.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student with ID %s is not in the records.";
    public static final String MESSAGE_LABEL_NOT_FOUND = "Student %1$s does not have label \"%2$s\".";
    public static final String MESSAGE_DUPLICATE_LABEL = "Student %1$s already has label \"%2$s\".";
    private static final Logger logger = LogsCenter.getLogger(EditCommand.class);

    private final StudentId targetStudentId;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * Creates a command targeting the student with {@code studentId}.
     * The descriptor is copied so later changes to it do not affect this command.
     *
     * @param studentId The Student ID of the student to edit.
     * @param editPersonDescriptor The details to edit.
     */
    public EditCommand(StudentId studentId, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(studentId);
        requireNonNull(editPersonDescriptor);

        this.targetStudentId = studentId;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    /**
     * {@inheritDoc}
     * Finds the student by ID in the complete address book, applies the requested changes,
     * and shows all students after a successful edit.
     *
     * @throws CommandException if the student is missing, a label change is invalid,
     *         or the edited student duplicates another student.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToEdit = findStudent(model);
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            logger.warning("Edit rejected: duplicate student ID.");
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assert model.hasPerson(editedPerson);
        logger.info("Edited student.");
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Finds the target student in the complete address book, including students hidden by a filter.
     *
     * @throws CommandException if no student has the target Student ID.
     */
    private Person findStudent(Model model) throws CommandException {
        assert model != null;
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getStudentId().equals(targetStudentId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("Edit rejected: student not found.");
                    return new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, targetStudentId));
                });
    }

    /**
     * Returns true because editing changes student data.
     */
    @Override
    public boolean isModifyingData() {
        return true;
    }

    /**
     * Creates a replacement student using the requested fields and retaining all others,
     * including the Student ID and attendance records.
     *
     * @throws CommandException if a requested label change is invalid.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor)
            throws CommandException {
        assert personToEdit != null;
        assert editPersonDescriptor != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        StudentId updatedStudentId = personToEdit.getStudentId();
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Remark updatedRemark = editPersonDescriptor.getRemark().orElse(personToEdit.getRemark());
        Set<Label> updatedLabels = updateLabels(personToEdit, editPersonDescriptor);

        return new Person(updatedName, updatedStudentId, updatedEmail,
                updatedLabels, updatedRemark, personToEdit.getAttendance());
    }

    /**
     * Returns the student's labels after clearing them or applying removals before additions.
     * Label matching ignores case, and the student's existing set is not modified.
     *
     * @throws CommandException if a label to remove is absent or a label to add is already present.
     */
    private static Set<Label> updateLabels(Person person, EditPersonDescriptor descriptor) throws CommandException {
        if (descriptor.isClearingLabels()) {
            return Set.of();
        }

        Set<Label> labels = new HashSet<>(person.getLabels());
        removeLabels(labels, descriptor.getLabelsToRemove(), person.getStudentId());
        addLabels(labels, descriptor.getLabelsToAdd(), person.getStudentId());
        return labels;
    }

    private static void removeLabels(Set<Label> labels, List<Label> toRemove, StudentId studentId)
            throws CommandException {
        assert labels != null;
        assert toRemove != null;
        assert studentId != null;

        for (Label labelToRemove : toRemove) {
            Label existingLabel = findMatchingLabel(labels, labelToRemove).orElseThrow(() -> {
                logger.warning("Edit rejected: label not assigned to student.");
                return new CommandException(String.format(MESSAGE_LABEL_NOT_FOUND,
                        studentId, labelToRemove.labelName));
            });
            labels.remove(existingLabel);
        }
    }

    private static void addLabels(Set<Label> labels, List<Label> toAdd, StudentId studentId)
            throws CommandException {
        assert labels != null;
        assert toAdd != null;
        assert studentId != null;

        for (Label labelToAdd : toAdd) {
            if (findMatchingLabel(labels, labelToAdd).isPresent()) {
                logger.warning("Edit rejected: duplicate label.");
                throw new CommandException(String.format(MESSAGE_DUPLICATE_LABEL,
                        studentId, labelToAdd.labelName));
            }
            labels.add(labelToAdd);
        }
    }

    /**
     * Finds a label with the same name as {@code target}, ignoring case.
     */
    private static Optional<Label> findMatchingLabel(Set<Label> labels, Label target) {
        return labels.stream()
                .filter(label -> label.equalsIgnoreCase(target))
                .findFirst();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return targetStudentId.equals(otherEditCommand.targetStudentId)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetStudentId", targetStudentId)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores replacement fields and label operations for editing a student.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Email email;
        private Remark remark;
        private List<Label> labelsToAdd = new ArrayList<>();
        private List<Label> labelsToRemove = new ArrayList<>();
        private boolean isClearingLabels;

        /**
         * Creates a descriptor with no requested changes.
         */
        public EditPersonDescriptor() {}

        /**
         * Creates an independent copy of the requested changes in {@code toCopy}.
         *
         * @param toCopy The descriptor to copy.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setEmail(toCopy.email);
            setRemark(toCopy.remark);
            setLabelsToAdd(toCopy.labelsToAdd);
            setLabelsToRemove(toCopy.labelsToRemove);
            setClearingLabels(toCopy.isClearingLabels);
        }

        /**
         * Returns whether any field replacement, label change, or label clear was requested.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, email, remark)
                    || !labelsToAdd.isEmpty() || !labelsToRemove.isEmpty() || isClearingLabels;
        }

        /**
         * Sets the replacement name, or null to leave the student's name unchanged.
         */
        public void setName(Name name) {
            this.name = name;
        }

        /**
         * Returns the replacement name, if one was requested.
         */
        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        /**
         * Sets the replacement email, or null to leave the student's email unchanged.
         */
        public void setEmail(Email email) {
            this.email = email;
        }

        /**
         * Returns the replacement email, if one was requested.
         */
        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        /**
         * Sets the replacement remark, or null to leave the student's remark unchanged.
         * An empty {@code Remark} clears the existing remark.
         */
        public void setRemark(Remark remark) {
            this.remark = remark;
        }

        /**
         * Returns the replacement remark, if one was requested.
         */
        public Optional<Remark> getRemark() {
            return Optional.ofNullable(remark);
        }

        /**
         * Copies the labels to add so later changes to {@code labelsToAdd} do not affect this descriptor.
         */
        public void setLabelsToAdd(List<Label> labelsToAdd) {
            this.labelsToAdd = new ArrayList<>(requireNonNull(labelsToAdd));
        }

        /**
         * Returns an unmodifiable copy of the requested label additions.
         */
        public List<Label> getLabelsToAdd() {
            return List.copyOf(labelsToAdd);
        }

        /**
         * Copies the labels to remove so later changes to {@code labelsToRemove} do not affect this descriptor.
         */
        public void setLabelsToRemove(List<Label> labelsToRemove) {
            this.labelsToRemove = new ArrayList<>(requireNonNull(labelsToRemove));
        }

        /**
         * Returns an unmodifiable copy of the requested label removals.
         */
        public List<Label> getLabelsToRemove() {
            return List.copyOf(labelsToRemove);
        }

        /**
         * Sets whether the edit should clear all of the student's labels.
         */
        public void setClearingLabels(boolean isClearingLabels) {
            this.isClearingLabels = isClearingLabels;
        }

        /**
         * Returns whether clearing all labels was requested.
         */
        public boolean isClearingLabels() {
            return isClearingLabels;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(remark, otherEditPersonDescriptor.remark)
                    && labelsToAdd.equals(otherEditPersonDescriptor.labelsToAdd)
                    && labelsToRemove.equals(otherEditPersonDescriptor.labelsToRemove)
                    && isClearingLabels == otherEditPersonDescriptor.isClearingLabels;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("email", email)
                    .add("remark", remark)
                    .add("labelsToAdd", labelsToAdd)
                    .add("labelsToRemove", labelsToRemove)
                    .add("isClearingLabels", isClearingLabels)
                    .toString();
        }
    }
}
