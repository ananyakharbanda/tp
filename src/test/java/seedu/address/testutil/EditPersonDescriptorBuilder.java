package seedu.address.testutil;

import java.util.Arrays;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.label.Label;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/**
 * A utility class to help with building EditPersonDescriptor objects.
 */
public class EditPersonDescriptorBuilder {

    private EditPersonDescriptor descriptor;

    public EditPersonDescriptorBuilder() {
        descriptor = new EditPersonDescriptor();
    }

    public EditPersonDescriptorBuilder(EditPersonDescriptor descriptor) {
        this.descriptor = new EditPersonDescriptor(descriptor);
    }

    /**
     * Returns an {@code EditPersonDescriptor} with fields containing {@code person}'s details
     */
    public EditPersonDescriptorBuilder(Person person) {
        descriptor = new EditPersonDescriptor();
        descriptor.setName(person.getName());
        descriptor.setEmail(person.getEmail());
        descriptor.setRemark(person.getRemark());
    }

    /**
     * Sets the {@code Name} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withName(String name) {
        descriptor.setName(new Name(name));
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withEmail(String email) {
        descriptor.setEmail(new Email(email));
        return this;
    }

    /**
     * Sets the {@code Remark} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withRemark(String remark) {
        descriptor.setRemark(new Remark(remark));
        return this;
    }

    /**
     * Adds labels to the descriptor's requested additions.
     */
    public EditPersonDescriptorBuilder withLabelsToAdd(String... labelNames) {
        descriptor.setLabelsToAdd(Arrays.stream(labelNames).map(Label::new).toList());
        return this;
    }

    /**
     * Adds labels to the descriptor's requested removals.
     */
    public EditPersonDescriptorBuilder withLabelsToRemove(String... labelNames) {
        descriptor.setLabelsToRemove(Arrays.stream(labelNames).map(Label::new).toList());
        return this;
    }

    /**
     * Requests removal of every label.
     */
    public EditPersonDescriptorBuilder withClearedLabels() {
        descriptor.setClearingLabels(true);
        return this;
    }

    public EditPersonDescriptor build() {
        return descriptor;
    }
}

