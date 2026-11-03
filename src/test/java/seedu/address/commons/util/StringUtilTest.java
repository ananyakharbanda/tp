package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    public void trimSpaces_preservesInternalCharacters() {
        String text = "Meet\u00a0at\u2009\u202f2pm\nNext\u3000week\u115f!";
        String padding = " \t\r\n\u00a0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
                + "\u2008\u2009\u200a\u202f\u205f\u3000\u115f\u1160\u3164\uffa0";
        assertEquals(text, StringUtil.trimSpaces(padding + text + padding));
        assertEquals(text, StringUtil.trimSpaces(text));
        assertEquals("", StringUtil.trimSpaces(padding));
        assertEquals("", StringUtil.trimSpaces(""));
        assertEquals("\u200btext\ufeff", StringUtil.trimSpaces("\u200btext\ufeff"));
        assertThrows(NullPointerException.class, () -> StringUtil.trimSpaces(null));
    }

    @Test
    public void trimSpaces_allCharacters_preservesExistingPaddingRules() {
        for (int value = Character.MIN_VALUE; value <= Character.MAX_VALUE; value++) {
            String padding = String.valueOf((char) value);
            String text = padding + "text" + padding;
            assertEquals(StringUtil.normaliseSpaces(text).trim(), StringUtil.trimSpaces(text),
                    "Padding character U+" + Integer.toHexString(value));
        }
    }

    @Test
    public void normaliseSpaces_unicodeSeparators_returnsAsciiSpaces() {
        // EP: Every Unicode space separator, including non-breaking spaces, is an ordinary space.
        String separators = " \u00a0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
                + "\u2008\u2009\u200a\u202f\u205f\u3000";
        assertEquals(" ".repeat(separators.length()), StringUtil.normaliseSpaces(separators));
        assertEquals("A  B", StringUtil.normaliseSpaces("A\u00a0\u2009B"));
    }

    @Test
    public void normaliseSpaces_hangulFillers_returnsAsciiSpaces() {
        // EP: Hangul leading, vowel, compatibility, and halfwidth fillers are treated as blank spaces.
        for (String filler : new String[] {"\u115f", "\u1160", "\u3164", "\uffa0"}) {
            // BVA: One filler maps to exactly one space; repeated fillers retain their count.
            assertEquals(" ", StringUtil.normaliseSpaces(filler));
            assertEquals("A  B", StringUtil.normaliseSpaces("A" + filler + filler + "B"));
            assertEquals(" \uac00 ", StringUtil.normaliseSpaces(filler + "\uac00" + filler));
        }
    }

    @Test
    public void normaliseSpaces_otherCharacters_preservesText() {
        // EP: Control whitespace, Unicode line separators, zero-width spaces, BOMs, and other text are preserved.
        String text = "Hello\tworld\r\n\u2028\u2029\u200b\ufeff\ud83d\ude00";
        assertEquals(text, StringUtil.normaliseSpaces(text));
        // BVA: Zero characters is valid; null is outside the accepted input partition.
        assertEquals("", StringUtil.normaliseSpaces(""));
        assertThrows(NullPointerException.class, () -> StringUtil.normaliseSpaces(null));
    }

    @Test
    public void contains_unicodeSpaces_matchesAsciiSpaces() {
        // EP: Search text and query words treat Unicode and ASCII spaces equivalently.
        assertTrue(StringUtil.containsWordIgnoreCase("Alice\u00a0Pauline", "\u202fPauline\u3000"));
        assertTrue(StringUtil.containsIgnoreCase("Follow\u2009up", "follow up"));
        assertTrue(StringUtil.containsIgnoreCase("Follow up", "follow\u00a0up"));
    }

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: empty strings
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary value
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: not a number
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: zero as prefix
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: signed numbers
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: number larger than Integer.MAX_VALUE
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString(Integer.MAX_VALUE + 1)));

        // EP: valid numbers, should return true
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary value
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for containsWordIgnoreCase --------------------------------------

    /*
     * Invalid equivalence partitions for word: null, empty, multiple words
     * Invalid equivalence partitions for sentence: null
     * The four test cases below test one invalid input at a time.
     */

    @Test
    public void containsWordIgnoreCase_nullWord_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase("typical sentence", null));
    }

    @Test
    public void containsWordIgnoreCase_emptyWord_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter cannot be empty", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "  "));
    }

    @Test
    public void containsWordIgnoreCase_multipleWords_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter should be a single word", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "aaa BBB"));
    }

    @Test
    public void containsWordIgnoreCase_nullSentence_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase(null, "abc"));
    }

    /*
     * Valid equivalence partitions for word:
     *   - any word
     *   - word containing symbols/numbers
     *   - word with leading/trailing spaces
     *
     * Valid equivalence partitions for sentence:
     *   - empty string
     *   - one word
     *   - multiple words
     *   - sentence with extra spaces
     *
     * Possible scenarios returning true:
     *   - matches first word in sentence
     *   - last word in sentence
     *   - middle word in sentence
     *   - matches multiple words
     *
     * Possible scenarios returning false:
     *   - query word matches part of a sentence word
     *   - sentence word matches part of the query word
     *
     * The test method below tries to verify all above with a reasonably low number of test cases.
     */

    @Test
    public void containsWordIgnoreCase_validInputs_correctResult() {

        // Empty sentence
        assertFalse(StringUtil.containsWordIgnoreCase("", "abc")); // Boundary case
        assertFalse(StringUtil.containsWordIgnoreCase("    ", "123"));

        // Matches a partial word only
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bb")); // Sentence word bigger than query word
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bbbb")); // Query word bigger than sentence word

        // Matches word in the sentence, different upper/lower case letters
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc", "Bbb")); // First word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc@1", "CCc@1")); // Last word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("  AAA   bBb   ccc  ", "aaa")); // Sentence has extra spaces
        assertTrue(StringUtil.containsWordIgnoreCase("Aaa", "aaa")); // Only one word in sentence (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "  ccc  ")); // Leading/trailing spaces

        // Matches multiple words in sentence
        assertTrue(StringUtil.containsWordIgnoreCase("AAA bBb ccc  bbb", "bbB"));
    }

    //---------------- Tests for containsIgnoreCase --------------------------------------

    @Test
    public void containsIgnoreCase_nullWord_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsIgnoreCase("typical sentence", null));
    }

    @Test
    public void containsIgnoreCase_emptyWord_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter cannot be empty", ()
            -> StringUtil.containsIgnoreCase("typical sentence", "  "));
    }

    @Test
    public void containsIgnoreCase_nullSentence_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsIgnoreCase(null, "abc"));
    }

    @Test
    public void containsIgnoreCase_validInputs_correctResult() {
        // Empty sentence
        assertFalse(StringUtil.containsIgnoreCase("", "abc"));
        assertFalse(StringUtil.containsIgnoreCase("    ", "123"));

        // Matches partial words
        assertTrue(StringUtil.containsIgnoreCase("aaa bbb ccc", "bb"));
        assertTrue(StringUtil.containsIgnoreCase("aaa bbb ccc", "aa"));
        assertFalse(StringUtil.containsIgnoreCase("aaa bbb ccc", "dddd"));

        // Matches different upper/lower case letters
        assertTrue(StringUtil.containsIgnoreCase("aaa bBb ccc", "Bbb"));
        assertTrue(StringUtil.containsIgnoreCase("aaa bBb ccc@1", "CCc"));
        assertTrue(StringUtil.containsIgnoreCase("  AAA   bBb   ccc  ", "aaa"));
        assertTrue(StringUtil.containsIgnoreCase("Aaa", "aa"));
        assertTrue(StringUtil.containsIgnoreCase("aaa bbb ccc", "  cc  "));
        assertTrue(StringUtil.containsIgnoreCase("aaa bbb ccc", "  aaa bb  "));
    }

    @Test
    public void containsIgnoreCase_fourLanguageExamples_matchesLiteralSubstring() {
        // EP: English text with a matching substring
        assertTrue(StringUtil.containsIgnoreCase("Needs help", "help"));

        // EP: Malay text with a matching substring
        assertTrue(StringUtil.containsIgnoreCase("Perlu bantuan", "bantuan"));

        // EP: Mandarin Chinese text with a matching substring
        assertTrue(StringUtil.containsIgnoreCase("需要帮助", "帮助"));

        // EP: Tamil text with a matching substring
        assertTrue(StringUtil.containsIgnoreCase("உதவி தேவை", "உதவி"));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
