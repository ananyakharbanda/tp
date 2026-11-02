package seedu.address.model.label;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class LabelTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null label
        assertThrows(NullPointerException.class, () -> new Label(null));
    }

    @Test
    public void constructor_invalidLabelName_throwsIllegalArgumentException() {
        // EP: empty label
        assertThrows(IllegalArgumentException.class, () -> new Label(""));
    }

    @Test
    public void constructor_validLabelName_storesLowercaseText() {
        // EP: valid mixed-case label with internal spaces
        Label label = new Label("Discrete Math Tutorial");

        assertEquals("discrete math tutorial", label.labelName);
    }

    @Test
    public void constructor_caseVariations_storeEqualLabels() {
        // EP: labels differing only in capitalization have the same stored identity
        Label lowercase = new Label("tut1");
        Label mixedCase = new Label("TuT1");

        assertEquals(lowercase, mixedCase);
        assertEquals(lowercase.hashCode(), mixedCase.hashCode());
        assertEquals("tut1", mixedCase.labelName);
    }

    @Test
    public void isValidLabelName_null_throwsNullPointerException() {
        // EP: null label
        assertThrows(NullPointerException.class, () -> Label.isValidLabelName(null));
    }

    @Test
    public void isValidLabelName_invalidLabelName_returnsFalse() {
        // EP: empty or whitespace-only label
        assertFalse(Label.isValidLabelName("")); // BVA: zero characters
        assertFalse(Label.isValidLabelName(" "));

        // EP: label exceeds the maximum length
        assertFalse(Label.isValidLabelName("a".repeat(Label.MAX_NAME_LENGTH + 1))); // BVA: one above maximum
        assertFalse(Label.isValidLabelName("İ".repeat(61))); // BVA: one above the legacy limit

        // EP: disallowed character or no letter or digit
        assertFalse(Label.isValidLabelName("Tutorial/1"));
        assertFalse(Label.isValidLabelName("Tutorial\n1"));
        assertFalse(Label.isValidLabelName("---"));
    }

    @Test
    public void isValidLabelName_validLabelName_returnsTrue() {
        // EP: valid label length
        assertTrue(Label.isValidLabelName("A")); // BVA: minimum valid length
        assertTrue(Label.isValidLabelName("a".repeat(Label.MAX_NAME_LENGTH - 1))); // BVA: one below maximum
        assertTrue(Label.isValidLabelName("a".repeat(Label.MAX_NAME_LENGTH))); // BVA: maximum length
        assertTrue(Label.isValidLabelName("İ".repeat(60))); // BVA: maximum before lowercase expansion
        assertTrue(Label.isValidLabelName("i\u0307".repeat(60))); // EP: persisted lowercase equivalent

        // EP: valid label with supported punctuation and spacing
        assertTrue(Label.isValidLabelName("Discrete Math Tutorial"));
        assertTrue(Label.isValidLabelName("Tutorial-1"));
        assertTrue(Label.isValidLabelName("Tutorial_1"));
        assertTrue(Label.isValidLabelName("Today's Tutorial"));
        assertTrue(Label.isValidLabelName("Tutorial (Monday)"));
        assertTrue(Label.isValidLabelName("Tutorial 1.2"));
    }

    @Test
    public void displayOrder_mixedCase_sortsByLowercaseName() {
        // EP: uppercase and lowercase names are normalized before alphabetical sorting
        List<String> sorted = Stream.of("beta", "Zeta", "alpha", "Beta", "Gamma")
                .map(Label::new)
                .sorted(Label.DISPLAY_ORDER)
                .map(label -> label.labelName)
                .toList();

        assertEquals(List.of("alpha", "beta", "beta", "gamma", "zeta"), sorted);
    }

    @Test
    public void equalsIgnoreCase_null_throwsNullPointerException() {
        Label label = new Label("Tutorial 1");
        assertThrows(NullPointerException.class, () -> label.equalsIgnoreCase((Label) null));
        assertThrows(NullPointerException.class, () -> label.equalsIgnoreCase((String) null));
    }

    @Test
    public void equalsIgnoreCase_matchingCaseVariations_returnsTrue() {
        Label label = new Label("Tutorial 1");

        // EP: identical string
        assertTrue(label.equalsIgnoreCase(new Label("Tutorial 1")));
        assertTrue(label.equalsIgnoreCase("Tutorial 1"));

        // EP: different casing
        assertTrue(label.equalsIgnoreCase(new Label("tutorial 1")));
        assertTrue(label.equalsIgnoreCase("TUTORIAL 1"));
    }

    @Test
    public void equalsIgnoreCase_differentLabels_returnsFalse() {
        Label label = new Label("Tutorial 1");

        // EP: different label
        assertFalse(label.equalsIgnoreCase(new Label("Tutorial 2")));
        assertFalse(label.equalsIgnoreCase("Tutorial 2"));
    }
}
