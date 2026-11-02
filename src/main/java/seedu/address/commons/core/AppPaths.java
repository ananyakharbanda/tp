package seedu.address.commons.core;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves application files beside the running JAR, or in the working directory for development runs.
 * This class must not use logging because logging initialization depends on these paths.
 */
public final class AppPaths {
    private static final Path APPLICATION_DIRECTORY = findApplicationDirectory();

    private AppPaths() {}

    /**
     * Returns the folder containing the running JAR, or the working directory when running compiled classes.
     */
    public static Path getApplicationDirectory() {
        return APPLICATION_DIRECTORY;
    }

    /**
     * Returns the absolute student-data file path.
     */
    public static Path getStudentDataFilePath() {
        return APPLICATION_DIRECTORY.resolve("data").resolve("addressbook.json");
    }

    /**
     * Returns the absolute preferences file path.
     */
    public static Path getPreferencesFilePath() {
        return APPLICATION_DIRECTORY.resolve("preferences.json");
    }

    /**
     * Returns the absolute log-file pattern, escaping percent signs used by FileHandler's pattern syntax.
     */
    public static String getLogFilePattern() {
        return APPLICATION_DIRECTORY.resolve("addressbook.log").toString().replace("%", "%%");
    }

    private static Path findApplicationDirectory() {
        try {
            Path location = Path.of(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath().normalize();
            if (Files.isRegularFile(location)) {
                return location.getParent();
            }
            return Path.of("").toAbsolutePath().normalize();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to determine the application folder.", e);
        }
    }
}
