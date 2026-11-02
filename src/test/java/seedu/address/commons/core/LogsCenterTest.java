package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {
    private static final String STARTUP_MESSAGE = "Startup logging is available.";
    private static final String LATER_MESSAGE = "Later logging is available.";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void initialization_writableLogFile_logsToConsoleAndFile() throws Exception {
        String output = runStartupProbe();

        assertTrue(output.contains(STARTUP_MESSAGE), output);
        assertTrue(output.contains(LATER_MESSAGE), output);
        String logContents = Files.readString(temporaryFolder.resolve("addressbook.log.0"));
        assertTrue(logContents.contains(STARTUP_MESSAGE), logContents);
        assertTrue(logContents.contains(LATER_MESSAGE), logContents);
    }

    @Test
    public void initialization_logFileIsDirectory_continuesWithConsoleLogging() throws Exception {
        Path blockedLogFile = Files.createDirectory(temporaryFolder.resolve("addressbook.log.0"));
        Path existingFile = Files.writeString(blockedLogFile.resolve("existing.txt"), "Keep this file.");

        String output = runStartupProbe();

        assertTrue(output.contains("Continuing with console logging."), output);
        assertTrue(output.contains(STARTUP_MESSAGE), output);
        assertTrue(output.contains(LATER_MESSAGE), output);
        assertTrue(Files.isDirectory(blockedLogFile));
        assertEquals("Keep this file.", Files.readString(existingFile));
    }

    /**
     * Runs logging initialization in a fresh JVM so every test exercises the static initializer.
     * The working directory keeps all log files within the temporary test folder.
     * Inherits the JaCoCo agent, when present, so child-process coverage reaches the same report.
     */
    private String runStartupProbe() throws Exception {
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");
        String classPath = Path.of(LogsCenter.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(LogsCenterTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path outputFile = temporaryFolder.resolve("startup-output.txt");
        List<String> command = new ArrayList<>();
        command.add(javaExecutable.toString());
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("-javaagent:") && argument.contains("jacoco")) {
                command.add(withAbsoluteCoverageDestination(argument));
            }
        }
        command.addAll(List.of("-cp", classPath, StartupProbe.class.getName()));
        Process process = new ProcessBuilder(command)
                .directory(temporaryFolder.toFile())
                .redirectErrorStream(true)
                .redirectOutput(outputFile.toFile())
                .start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Logging initialization timed out.");
            String output = Files.readString(outputFile);
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }

    /**
     * Keeps coverage in the parent test's report even though the child runs in a different working directory.
     */
    private String withAbsoluteCoverageDestination(String agentArgument) {
        Matcher destination = Pattern.compile("destfile=([^,]+)").matcher(agentArgument);
        if (!destination.find()) {
            return agentArgument;
        }
        String absoluteDestination = "destfile=" + Path.of(destination.group(1)).toAbsolutePath().normalize();
        return destination.replaceFirst(Matcher.quoteReplacement(absoluteDestination));
    }

    /**
     * Exercises first-time initialization and subsequent logger requests in a separate process.
     */
    public static class StartupProbe {
        /**
         * Logs through both supported logger factories.
         *
         * @param args Unused command-line arguments.
         */
        public static void main(String[] args) {
            LogsCenter.getLogger(StartupProbe.class).info(STARTUP_MESSAGE);
            LogsCenter.getLogger("LaterLogger").info(LATER_MESSAGE);
        }
    }
}
