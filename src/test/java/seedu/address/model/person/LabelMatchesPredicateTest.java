package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.model.label.Label;
import seedu.address.testutil.PersonBuilder;

public class LabelMatchesPredicateTest {
    @Test
    public void constructor_nullLabel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LabelMatchesPredicate(null));
    }

    @Test
    public void test_completeLabelMatchesIgnoringCase_returnsTrue() {
        // EP: complete label matches ignoring case, including among multiple labels.
        LabelMatchesPredicate predicate = new LabelMatchesPredicate(new Label("Discrete Math Tutorial"));
        assertTrue(predicate.test(new PersonBuilder().withLabels("Discrete Math Tutorial").build()));
        assertTrue(predicate.test(new PersonBuilder().withLabels("discrete math tutorial").build()));
        assertTrue(predicate.test(new PersonBuilder().withLabels("Other Group", "DISCRETE MATH TUTORIAL").build()));
    }

    @Test
    public void test_unicodeLettersWithDifferentRootLowercaseForms_doNotMatch() {
        // EP: Unicode labels with different root lowercase forms remain distinct.
        LabelMatchesPredicate latinI = new LabelMatchesPredicate(new Label("I"));
        assertFalse(latinI.test(new PersonBuilder().withLabels("ı").build()));
        assertFalse(latinI.test(new PersonBuilder().withLabels("İ").build()));

        LabelMatchesPredicate greekSigma = new LabelMatchesPredicate(new Label("ΟΣ"));
        assertTrue(greekSigma.test(new PersonBuilder().withLabels("ος").build()));
        assertFalse(greekSigma.test(new PersonBuilder().withLabels("οσ").build()));
    }

    @Test
    public void test_uppercaseDottedI_matchesItsExpandedRootLowercaseForm() {
        // EP: Unicode lowercase expansion matches in both directions.
        LabelMatchesPredicate uppercase = new LabelMatchesPredicate(new Label("İ"));
        assertTrue(uppercase.test(new PersonBuilder().withLabels("i\u0307").build()));
        LabelMatchesPredicate lowercase = new LabelMatchesPredicate(new Label("i\u0307"));
        assertTrue(lowercase.test(new PersonBuilder().withLabels("İ").build()));
    }

    @Test
    public void test_defaultLocaleDoesNotChangeLabelMatching() {
        // EP: default locale must not affect root-locale label comparison.
        Locale originalLocale = Locale.getDefault();
        try {
            for (Locale locale : List.of(Locale.ENGLISH, Locale.forLanguageTag("tr-TR"))) {
                Locale.setDefault(locale);
                LabelMatchesPredicate predicate = new LabelMatchesPredicate(new Label("I"));
                assertTrue(predicate.test(new PersonBuilder().withLabels("i").build()));
                assertFalse(predicate.test(new PersonBuilder().withLabels("ı").build()));
            }
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void test_labelMustMatchCompletely_returnsFalse() {
        // EP: partial names, unrelated names, and absent labels do not match.
        LabelMatchesPredicate predicate = new LabelMatchesPredicate(new Label("Tutorial"));
        assertFalse(predicate.test(new PersonBuilder().withLabels("Tutorial 1").build()));
        assertFalse(predicate.test(new PersonBuilder().withLabels("Discrete Math Tutorial").build()));
        assertFalse(predicate.test(new PersonBuilder().withLabels("Other Group").build()));
        assertFalse(predicate.test(new PersonBuilder().withLabels().build()));
    }

    @Test
    public void test_internalLabelSpacingIsPreserved() {
        // EP: equal internal spacing matches; different internal spacing does not.
        LabelMatchesPredicate predicate = new LabelMatchesPredicate(new Label("Tutorial  1"));
        assertTrue(predicate.test(new PersonBuilder().withLabels("Tutorial  1").build()));
        assertFalse(predicate.test(new PersonBuilder().withLabels("Tutorial 1").build()));
    }

    @Test
    public void equals() {
        LabelMatchesPredicate predicate = new LabelMatchesPredicate(new Label("Tutorial 1"));
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new LabelMatchesPredicate(new Label("Tutorial 1"))));
        assertFalse(predicate.equals(new LabelMatchesPredicate(new Label("Tutorial 2"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("Tutorial 1"));
    }

    @Test
    public void toStringMethod() {
        Label label = new Label("Tutorial 1");
        assertEquals(LabelMatchesPredicate.class.getCanonicalName() + "{label=" + label + "}",
                new LabelMatchesPredicate(label).toString());
    }
}
