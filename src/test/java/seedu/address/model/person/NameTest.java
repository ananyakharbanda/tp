package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        // EP: invalid name
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // EP: null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // EP: empty or whitespace-only name
        assertFalse(Name.isValidName("")); // BVA: zero characters
        assertFalse(Name.isValidName(" ")); // spaces only

        // EP: unsupported symbol, digit, or control character
        assertFalse(Name.isValidName("^")); // unsupported symbol
        assertFalse(Name.isValidName("peter*")); // unsupported symbol
        assertFalse(Name.isValidName("12345"));
        assertFalse(Name.isValidName("peter the 2nd"));

        // EP: allowed punctuation or combining mark without a letter
        assertFalse(Name.isValidName(" .-'’ ")); // no letter
        assertFalse(Name.isValidName("\u0301")); // combining mark without a letter

        // EP: name contains a control character or slash
        assertFalse(Name.isValidName("John\tDoe"));
        assertFalse(Name.isValidName("John\nDoe"));
        assertFalse(Name.isValidName("John/Doe"));

        // EP: valid names with letters, separators, and combining marks
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr."));
        assertTrue(Name.isValidName("Anne-Marie O'Neill"));
        assertTrue(Name.isValidName("D’Arcy"));
        assertTrue(Name.isValidName("José 王小明"));
        assertTrue(Name.isValidName("Jose\u0301"));

        // EP: English name using valid Latin letters
        assertTrue(Name.isValidName("Alex Tan"));

        // EP: Malay name using valid Latin letters
        assertTrue(Name.isValidName("Nur Aisyah"));

        // EP: Mandarin name in Chinese characters
        assertTrue(Name.isValidName("王小明"));

        // EP: Tamil-script name with combining marks
        assertTrue(Name.isValidName("அருண்"));
    }

    @Test
    public void constructor_surroundingWhitespaceAndRepeatedSpaces_normalisesName() {
        // EP: surrounding whitespace and repeated spaces
        assertEquals("John Doe", new Name(" \tJohn   Doe\r\n ").fullName);
        assertEquals("jOhN Doe", new Name(" jOhN   Doe ").fullName);
        assertEquals(new Name("John Doe"), new Name(" John   Doe "));
        assertEquals(new Name("John Doe").hashCode(), new Name(" John   Doe ").hashCode());
    }

    @Test
    public void isValidName_lengthBoundary_checksNormalisedLength() {
        // EP: valid name length
        assertTrue(Name.isValidName("A")); // BVA: minimum valid length
        assertTrue(Name.isValidName("A".repeat(Name.MAX_LENGTH - 1))); // BVA: one below maximum
        assertTrue(Name.isValidName("A".repeat(Name.MAX_LENGTH))); // BVA: maximum length
        String paddedName = "  " + "A".repeat(Name.MAX_LENGTH - 2) + "   B  ";
        assertEquals("A".repeat(Name.MAX_LENGTH - 2) + " B", new Name(paddedName).fullName);

        // EP: name exceeds the maximum length
        assertFalse(Name.isValidName("A".repeat(Name.MAX_LENGTH + 1))); // BVA: one above maximum
        assertThrows(IllegalArgumentException.class, Name.MESSAGE_CONSTRAINTS, ()
            -> new Name("A".repeat(Name.MAX_LENGTH + 1)));
    }

    @Test
    public void isValidName_chineseLengthBoundary_countsCodePoints() {
        // EP: valid Mandarin Chinese name
        assertTrue(Name.isValidName("王".repeat(Name.MAX_LENGTH - 1))); // BVA: 99 code points
        assertTrue(Name.isValidName("王".repeat(Name.MAX_LENGTH))); // BVA: 100 code points

        // EP: Mandarin Chinese name exceeds the maximum length
        assertFalse(Name.isValidName("王".repeat(Name.MAX_LENGTH + 1))); // BVA: 101 code points
    }

    @Test
    public void isValidName_tamilLengthBoundary_countsCombiningMarks() {
        // EP: valid Tamil name; each கி contains a letter and a combining mark
        String belowLimit = "கி".repeat(49) + "க";
        String atLimit = "கி".repeat(50);
        assertTrue(Name.isValidName(belowLimit)); // BVA: 99 code points
        assertTrue(Name.isValidName(atLimit)); // BVA: 100 code points

        // EP: Tamil name exceeds the maximum length
        assertFalse(Name.isValidName(atLimit + "க")); // BVA: 101 code points
    }

    @Test
    public void isValidName_supplementaryLetters_countsCodePoints() {
        // EP: supplementary letters
        // BVA: maximum number of Unicode code points
        String name = "\uD801\uDC00".repeat(Name.MAX_LENGTH);
        assertTrue(Name.isValidName(name));
        assertEquals(name, new Name(name).fullName);
        assertFalse(Name.isValidName(name + "A")); // BVA: one code point over the maximum
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // EP: same value
        assertTrue(name.equals(new Name("Valid Name")));

        // EP: same object
        assertTrue(name.equals(name));

        // EP: null
        assertFalse(name.equals(null));

        // EP: different type
        assertFalse(name.equals(5.0f));

        // EP: different name
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
