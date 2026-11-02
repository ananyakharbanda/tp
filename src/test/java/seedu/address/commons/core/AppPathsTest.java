package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppPathsTest {
    private static final String STUDENT_DATA = "Student records beside the JAR.";
    private static final String PREFERENCES = "Preferences beside the JAR.";
    private static final String LOG_MESSAGE = "Logging beside the JAR.";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void runningFromClasses_usesWorkingDirectory() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        assertEquals(workingDirectory, AppPaths.getApplicationDirectory());
        assertEquals(workingDirectory.resolve("data/addressbook.json"), AppPaths.getStudentDataFilePath());
        assertEquals(workingDirectory.resolve("preferences.json"), AppPaths.getPreferencesFilePath());
    }

    @Test
    public void runningFromJar_inAnotherDirectory_keepsFilesBesideJar() throws Exception {
        assertFilesBesideJar("installation", false);
    }

    @Test
    public void runningFromJar_withSpacesUnicodeAndPercentSigns_keepsFilesBesideJar() throws Exception {
        assertFilesBesideJar("Teach Assist Ω 100% %h %t %g %u %%", true);
    }

    @Test
    public void runningFromJar_blockedLogFile_continuesWithConsoleLogging() throws Exception {
        Path installationFolder = Files.createDirectory(temporaryFolder.resolve("installation"));
        Path jar = createProbeJar(installationFolder.resolve("Teach Assist.jar"));
        Path launchFolder = Files.createDirectory(temporaryFolder.resolve("launch-folder"));
        Path blockedLogFile = Files.createDirectory(installationFolder.resolve("addressbook.log.0"));
        Path existingFile = Files.writeString(blockedLogFile.resolve("existing.txt"), "Keep this file.");

        String output = runProbe(jar, launchFolder, false);

        assertTrue(output.contains("Continuing with console logging."), output);
        assertTrue(output.contains(LOG_MESSAGE), output);
        assertTrue(Files.isDirectory(blockedLogFile));
        assertEquals("Keep this file.", Files.readString(existingFile));
        assertEquals(STUDENT_DATA, Files.readString(installationFolder.resolve("data/addressbook.json")));
        assertEquals(PREFERENCES, Files.readString(installationFolder.resolve("preferences.json")));
        try (var files = Files.list(launchFolder)) {
            assertEquals(0, files.count());
        }
    }

    private void assertFilesBesideJar(String installationFolderName, boolean useUriLauncher) throws Exception {
        Path installationFolder = Files.createDirectory(temporaryFolder.resolve(installationFolderName));
        Path jar = createProbeJar(installationFolder.resolve("Teach Assist.jar"));
        Path launchFolder = Files.createDirectory(temporaryFolder.resolve("launch-folder"));
        Files.createDirectories(launchFolder.resolve("data"));
        Files.writeString(launchFolder.resolve("data/addressbook.json"), "Unrelated student data.");
        Files.writeString(launchFolder.resolve("preferences.json"), "Unrelated preferences.");

        runProbe(jar, launchFolder, useUriLauncher);
        Path secondLaunchFolder = Files.createDirectory(temporaryFolder.resolve("second-launch-folder"));
        runProbe(jar, secondLaunchFolder, useUriLauncher);

        assertEquals(STUDENT_DATA, Files.readString(installationFolder.resolve("data/addressbook.json")));
        assertEquals(PREFERENCES, Files.readString(installationFolder.resolve("preferences.json")));
        String logs = Files.readString(installationFolder.resolve("addressbook.log.0"));
        assertTrue(logs.contains(LOG_MESSAGE), logs);
        assertEquals("Unrelated student data.", Files.readString(launchFolder.resolve("data/addressbook.json")));
        assertEquals("Unrelated preferences.", Files.readString(launchFolder.resolve("preferences.json")));
        assertFalse(Files.exists(launchFolder.resolve("addressbook.log.0")));
        try (var files = Files.list(secondLaunchFolder)) {
            assertEquals(0, files.count());
        }
    }

    private Path createProbeJar(Path jar) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, StartupProbe.class.getName());
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            for (Class<?> type : new Class<?>[] {AppPaths.class, LogsCenter.class, StartupProbe.class}) {
                String entry = type.getName().replace('.', '/') + ".class";
                output.putNextEntry(new JarEntry(entry));
                try (InputStream input = type.getResourceAsStream("/" + entry)) {
                    input.transferTo(output);
                }
                output.closeEntry();
            }
        }
        return jar;
    }

    private String runProbe(Path jar, Path launchFolder, boolean useUriLauncher) throws Exception {
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");
        Path outputFile = temporaryFolder.resolve("probe-output.txt");
        List<String> command = new ArrayList<>();
        command.add(javaExecutable.toString());
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("-javaagent:") && argument.contains("jacoco")) {
                command.add(withAbsoluteCoverageDestination(argument));
            }
        }
        if (useUriLauncher) {
            // Avoid the Windows Java launcher's native encoding of non-ASCII command-line paths.
            String launcherClassPath = Path.of(AppPathsTest.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toString();
            command.addAll(List.of("-cp", launcherClassPath, JarLauncher.class.getName(), jar.toUri().toASCIIString()));
        } else {
            command.addAll(List.of("-jar", jar.toString()));
        }
        Process process = new ProcessBuilder(command)
                .directory(launchFolder.toFile()).redirectErrorStream(true)
                .redirectOutput(outputFile.toFile()).start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "File-location probe timed out.");
            String output = Files.readString(outputFile);
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }

    /**
     * Records child-process coverage in the parent report rather than the temporary launch directory.
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
     * Loads the packaged probe through an ASCII URI so tests do not depend on the Windows native code page.
     */
    public static class JarLauncher {
        /**
         * Invokes the JAR's declared entry point in an isolated class loader, preserving its code-source location.
         *
         * @param args The ASCII URI of the probe JAR.
         * @throws Exception if the JAR cannot be loaded or its entry point fails.
         */
        public static void main(String[] args) throws Exception {
            URI jarUri = URI.create(args[0]);
            String mainClass;
            try (JarFile jar = new JarFile(Path.of(jarUri).toFile())) {
                mainClass = jar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            }
            try (URLClassLoader loader = new URLClassLoader(new URL[] {jarUri.toURL()},
                    ClassLoader.getPlatformClassLoader())) {
                Class<?> entryPoint = Class.forName(mainClass, true, loader);
                entryPoint.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
            }
        }
    }

    /**
     * Exercises paths and logging from a packaged JAR without requiring a graphical environment.
     */
    public static class StartupProbe {
        /**
         * Creates application files, or checks the files created by a previous launch.
         *
         * @param args Unused command-line arguments.
         * @throws IOException if an application file cannot be accessed.
         */
        public static void main(String[] args) throws IOException {
            Path data = AppPaths.getStudentDataFilePath();
            Files.createDirectories(data.getParent());
            checkOrCreate(data, STUDENT_DATA);
            checkOrCreate(AppPaths.getPreferencesFilePath(), PREFERENCES);
            LogsCenter.getLogger("AppPathsProbe").info(LOG_MESSAGE);
        }

        private static void checkOrCreate(Path file, String expected) throws IOException {
            if (Files.exists(file)) {
                if (!expected.equals(Files.readString(file))) {
                    throw new AssertionError("Loaded application files from the wrong folder: " + file);
                }
            } else {
                Files.writeString(file, expected);
            }
        }
    }
}
