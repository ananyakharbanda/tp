package seedu.address.model.label;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Comparator;
import java.util.Locale;

/**
 * Represents a group label assigned to a student.
 * Guarantees: immutable; name is valid as declared in {@link #isValidLabelName(String)}
 */
public class Label {

    public static final int MAX_NAME_LENGTH = 60;
    public static final String MESSAGE_CONSTRAINTS = "Label names should be 1 to 60 characters long, "
            + "contain at least one letter or digit, and only contain letters, digits, spaces, hyphens, "
            + "underscores, apostrophes, parentheses, and periods.";

    /** Display order: by the stored lowercase name. */
    public static final Comparator<Label> DISPLAY_ORDER = Comparator.comparing(label -> label.labelName);

    public final String labelName;

    /**
     * Constructs a {@code Label}, storing its name in lowercase.
     *
     * @param labelName A valid label name.
     */
    public Label(String labelName) {
        requireNonNull(labelName);
        checkArgument(isValidLabelName(labelName), MESSAGE_CONSTRAINTS);
        this.labelName = labelName.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string remains a valid label name after lowercasing.
     */
    public static boolean isValidLabelName(String test) {
        requireNonNull(test);
        String lowercasedName = test.toLowerCase(Locale.ROOT);

        if (lowercasedName.isBlank()
                || lowercasedName.codePointCount(0, lowercasedName.length()) > MAX_NAME_LENGTH) {
            return false;
        }

        boolean hasLetterOrDigit = false;
        for (int i = 0; i < lowercasedName.length(); i += Character.charCount(lowercasedName.codePointAt(i))) {
            int codePoint = lowercasedName.codePointAt(i);
            if (!isAllowedCodePoint(codePoint)) {
                return false;
            }
            hasLetterOrDigit = hasLetterOrDigit || Character.isLetterOrDigit(codePoint);
        }
        return hasLetterOrDigit;
    }

    private static boolean isAllowedCodePoint(int codePoint) {
        return Character.isLetterOrDigit(codePoint)
                || isCombiningMark(codePoint)
                || Character.getType(codePoint) == Character.SPACE_SEPARATOR
                || codePoint == '-'
                || codePoint == '_'
                || codePoint == '\''
                || codePoint == '('
                || codePoint == ')'
                || codePoint == '.';
    }

    private static boolean isCombiningMark(int codePoint) {
        int characterType = Character.getType(codePoint);
        return characterType == Character.NON_SPACING_MARK
                || characterType == Character.COMBINING_SPACING_MARK
                || characterType == Character.ENCLOSING_MARK;
    }

    /**
     * Returns true if both labels have the same name, ignoring case with {@link Locale#ROOT}.
     *
     * @param other The other non-null label to compare against.
     * @return True if both label names match ignoring case.
     */
    public boolean equalsIgnoreCase(Label other) {
        requireNonNull(other);
        return equalsIgnoreCase(other.labelName);
    }

    /**
     * Returns true if this label's name equals {@code otherName}, ignoring case with {@link Locale#ROOT}.
     *
     * @param otherName The other non-null label name to compare against.
     * @return True if the names match ignoring case.
     */
    public boolean equalsIgnoreCase(String otherName) {
        requireNonNull(otherName);
        return labelName.toLowerCase(Locale.ROOT).equals(otherName.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Label otherLabel)) {
            return false;
        }

        return labelName.equals(otherLabel.labelName);
    }

    @Override
    public int hashCode() {
        return labelName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + labelName + ']';
    }
}
