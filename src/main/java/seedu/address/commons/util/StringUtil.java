package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Locale;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Converts Unicode space separators and Hangul fillers to ASCII spaces, preserving their number and position.
     * Hangul fillers U+115F, U+1160, U+3164, and U+FFA0 are treated as blank characters.
     * Tabs, line breaks, zero-width spaces, and byte order marks are preserved.
     *
     * @param text The non-null text to normalise.
     * @return Text with every Unicode space separator and supported filler replaced by an ASCII space.
     */
    public static String normaliseSpaces(String text) {
        requireNonNull(text);
        return text.replaceAll("[\\p{Zs}\\u115F\\u1160\\u3164\\uFFA0]", " ");
    }

    /**
     * Trims surrounding ASCII control whitespace, Unicode space separators, and supported Hangul fillers.
     * Preserves all characters inside the text, including Unicode spaces and line breaks.
     *
     * @param text The non-null text to trim.
     * @return Text with the same surrounding padding removed as {@code normaliseSpaces(text).trim()}.
     */
    public static String trimSpaces(String text) {
        requireNonNull(text);
        int start = 0;
        int end = text.length();
        while (start < end && isTrimmedSpace(text.charAt(start))) {
            start++;
        }
        while (end > start && isTrimmedSpace(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(start, end);
    }

    private static boolean isTrimmedSpace(char character) {
        return character <= ' ' || Character.getType(character) == Character.SPACE_SEPARATOR
                || character == '\u115f' || character == '\u1160'
                || character == '\u3164' || character == '\uffa0';
    }

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = normaliseSpaces(word).trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = normaliseSpaces(sentence);
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, and partial word matches are accepted.
     *   <br>examples:<pre>
     *       containsIgnoreCase("ABc def", "abc") == true
     *       containsIgnoreCase("ABc def", "DEF") == true
     *       containsIgnoreCase("ABc def", "AB") == true
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = normaliseSpaces(word).trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");

        return normaliseSpaces(sentence).toLowerCase(Locale.ROOT).contains(preppedWord.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
