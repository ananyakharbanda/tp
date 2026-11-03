package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_lengthBoundary_acceptsLimitAndRejectsOverflow() {
        // EP: valid remark at the maximum length
        // BVA: 4000 characters
        String atLimit = "x".repeat(Remark.MAX_LENGTH);
        assertEquals(atLimit, new Remark(atLimit).value);

        // EP: remark exceeds the maximum length
        // BVA: 4001 characters
        assertThrows(IllegalArgumentException.class, Remark.MESSAGE_CONSTRAINTS, () ->
                new Remark(atLimit + "x"));
    }

    @Test
    public void isValidRemark_chineseLengthBoundary_countsCodePoints() {
        // EP: valid Mandarin Chinese remark
        assertTrue(Remark.isValidRemark("中".repeat(Remark.MAX_LENGTH - 1))); // BVA: 3999 code points
        assertTrue(Remark.isValidRemark("中".repeat(Remark.MAX_LENGTH))); // BVA: 4000 code points

        // EP: Mandarin Chinese remark exceeds the maximum length
        assertFalse(Remark.isValidRemark("中".repeat(Remark.MAX_LENGTH + 1))); // BVA: 4001 code points
    }

    @Test
    public void isValidRemark_tamilLengthBoundary_countsCombiningMarks() {
        // EP: valid Tamil remark; each கி contains a letter and a combining mark
        String belowLimit = "கி".repeat(1999) + "க";
        String atLimit = "கி".repeat(2000);
        assertTrue(Remark.isValidRemark(belowLimit)); // BVA: 3999 code points
        assertTrue(Remark.isValidRemark(atLimit)); // BVA: 4000 code points

        // EP: Tamil remark exceeds the maximum length
        assertFalse(Remark.isValidRemark(atLimit + "க")); // BVA: 4001 code points
    }

    @Test
    public void isValidRemark_unicode_countsCodePoints() {
        // EP: supplementary Unicode characters at the code-point limit
        String atLimit = "\uD83D\uDE00".repeat(Remark.MAX_LENGTH);
        assertTrue(Remark.isValidRemark(atLimit));
        assertEquals(atLimit, new Remark(atLimit).value);
        // EP: remark exceeds the code-point limit
        assertFalse(Remark.isValidRemark(atLimit + "x")); // BVA: one code point over the maximum

        // EP: empty remark
        assertTrue(Remark.isValidRemark("")); // BVA: zero characters

        // EP: null remark
        assertThrows(NullPointerException.class, () -> Remark.isValidRemark(null));
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_anyText_preservesValue() {
        // EP: empty, whitespace-only, ordinary, and multi-line remarks
        for (String text : new String[] {"", " ", "Needs help with recursion", "Follow up: 你好!\nNext week"}) {
            assertTrue(Remark.isValidRemark(text));
            assertEquals(text, new Remark(text).value);
            assertEquals(text, new Remark(text).toString());
        }
    }

    @Test
    public void constructor_fourLanguageExamples_preservesValue() {
        // EP: valid English free-text remark
        assertEquals("Needs help", new Remark("Needs help").value);

        // EP: valid Malay free-text remark
        assertEquals("Perlu bantuan", new Remark("Perlu bantuan").value);

        // EP: valid Mandarin Chinese free-text remark
        assertEquals("需要帮助", new Remark("需要帮助").value);

        // EP: valid Tamil free-text remark
        assertEquals("உதவி தேவை", new Remark("உதவி தேவை").value);
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Needs help");
        Remark copy = new Remark("Needs help");
        // EP: same object
        assertTrue(remark.equals(remark));
        // EP: same value
        assertTrue(remark.equals(copy));
        assertEquals(remark.hashCode(), copy.hashCode());
        // EP: null
        assertFalse(remark.equals(null));
        // EP: different type
        assertFalse(remark.equals("Needs help"));
        // EP: different remark
        assertFalse(remark.equals(new Remark("Different remark")));
    }
}
