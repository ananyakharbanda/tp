package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.label.Label;

/**
 * Tests complete label names after Locale.ROOT lowercasing, consistently with editing and storage.
 */
public class LabelMatchesPredicate implements Predicate<Person> {
    private final Label label;

    /**
     * Creates a predicate matching any of a student's labels against {@code label}.
     *
     * @param label The non-null, validated label to match.
     */
    public LabelMatchesPredicate(Label label) {
        this.label = requireNonNull(label);
    }

    @Override
    public boolean test(Person person) {
        String normalizedLabelName = label.labelName.toLowerCase(Locale.ROOT);
        return person.getLabels().stream()
                .anyMatch(existing -> existing.labelName.toLowerCase(Locale.ROOT).equals(normalizedLabelName));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LabelMatchesPredicate otherPredicate)) {
            return false;
        }
        return label.equals(otherPredicate.label);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("label", label).toString();
    }
}
