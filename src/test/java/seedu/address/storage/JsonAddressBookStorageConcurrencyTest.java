package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;

public class JsonAddressBookStorageConcurrencyTest {
    @TempDir
    public Path testFolder;

    @Test
    public void saveAddressBook_anotherInstanceSaved_preservesNewerData() throws Exception {
        Path file = testFolder.resolve("students.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(file);
        first.saveAddressBook(getTypicalAddressBook());
        JsonAddressBookStorage second = new JsonAddressBookStorage(file);
        second.readAddressBook();

        AddressBook updated = getTypicalAddressBook();
        updated.addPerson(HOON);
        first.saveAddressBook(updated);
        String latestContents = Files.readString(file);

        assertThrows(DataFileChangedException.class, () -> second.saveAddressBook(new AddressBook()));
        assertEquals(latestContents, Files.readString(file));
        assertThrows(DataFileChangedException.class, () -> second.saveAddressBook(new AddressBook()));

        AddressBook reloaded = new AddressBook(second.readAddressBook().orElseThrow());
        second.saveAddressBook(reloaded);
        assertEquals(updated, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_fileCreatedAfterMissingRead_preservesNewFile() throws Exception {
        Path file = testFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        assertFalse(storage.readAddressBook().isPresent());
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        String latestContents = Files.readString(file);

        assertThrows(DataFileChangedException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertEquals(latestContents, Files.readString(file));
    }

    @Test
    public void saveAddressBook_fileDeletedAfterRead_doesNotRecreateFile() throws Exception {
        Path file = testFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        storage.readAddressBook();
        Files.delete(file);

        assertThrows(DataFileChangedException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertFalse(Files.exists(file));
    }

    @Test
    public void saveAddressBook_externalEditWithSameSizeAndTimestamp_preservesEdit() throws Exception {
        Path file = testFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        var timestamp = Files.getLastModifiedTime(file);
        // Keep the length identical so a size/mtime-only check would miss the change.
        String editedContents = Files.readString(file).replace("Alice", "Alise");
        Files.writeString(file, editedContents);
        Files.setLastModifiedTime(file, timestamp);

        assertThrows(DataFileChangedException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertEquals(editedContents, Files.readString(file));
    }

    @Test
    public void saveAddressBook_existingFileNeverRead_doesNotOverwriteFile() throws Exception {
        Path file = testFolder.resolve("students.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        String originalContents = Files.readString(file);

        assertThrows(DataFileChangedException.class, () ->
                new JsonAddressBookStorage(file).saveAddressBook(new AddressBook()));
        assertEquals(originalContents, Files.readString(file));
    }

    @Test
    public void saveAddressBook_replacementFails_canRetryWithoutReloading() throws Exception {
        Path file = testFolder.resolve("students.json");
        new JsonAddressBookStorage(file).saveAddressBook(getTypicalAddressBook());
        AtomicBoolean failReplacement = new AtomicBoolean(true);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
                if (failReplacement.get()) {
                    throw new IOException("Simulated replacement failure");
                }
                super.replaceDataFile(temporaryFile, targetFile);
            }
        };
        storage.readAddressBook();
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));

        failReplacement.set(false);
        storage.saveAddressBook(new AddressBook());
        assertEquals(new AddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_differentPaths_tracksEachFileSeparately() throws Exception {
        Path firstFile = testFolder.resolve("first.json");
        Path secondFile = testFolder.resolve("second.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(firstFile);
        storage.saveAddressBook(getTypicalAddressBook());
        storage.saveAddressBook(new AddressBook(), secondFile);
        Files.writeString(firstFile, "externally changed");

        storage.saveAddressBook(getTypicalAddressBook(), secondFile);
        assertThrows(DataFileChangedException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertEquals("externally changed", Files.readString(firstFile));
        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(secondFile).readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_otherProcessHoldsLock_refusesWriteThenSucceeds() throws Exception {
        Path file = testFolder.resolve("students.json");
        Path lockFile = testFolder.resolve("students.json.lock");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(getTypicalAddressBook());
        String originalContents = Files.readString(file);
        Process process = startLockProbe(lockFile);
        try {
            assertEquals("LOCKED", readProbeResult(process));
            assertThrows(DataFileInUseException.class, () -> storage.saveAddressBook(new AddressBook()));
            assertEquals(originalContents, Files.readString(file));
            process.getOutputStream().close();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Lock probe did not exit.");
            assertEquals(0, process.exitValue());

            // A persistent lock file is not itself a lock and must not prevent a later save.
            assertTrue(Files.exists(lockFile));
            storage.saveAddressBook(new AddressBook());
            assertEquals(new AddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        } finally {
            process.destroyForcibly();
            process.waitFor(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void saveAddressBook_duringReplacement_otherProcessCannotAcquireLock() throws Exception {
        Path file = testFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file) {
            @Override
            protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
                try {
                    Process process = startLockProbe(testFolder.resolve("students.json.lock"));
                    try {
                        assertEquals("BUSY", readProbeResult(process));
                        assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Lock probe did not exit.");
                        assertEquals(0, process.exitValue());
                    } finally {
                        process.destroyForcibly();
                        process.waitFor(10, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    throw new IOException("Unable to run lock probe", e);
                }
                super.replaceDataFile(temporaryFile, targetFile);
            }
        };

        storage.saveAddressBook(getTypicalAddressBook());
        storage.saveAddressBook(new AddressBook());
        assertEquals(new AddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    private Process startLockProbe(Path lockFile) throws Exception {
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");
        String classPath = Path.of(LockProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toString();
        return new ProcessBuilder(javaExecutable.toString(), "-cp", classPath, LockProbe.class.getName(),
                lockFile.toUri().toASCIIString()).redirectError(ProcessBuilder.Redirect.INHERIT).start();
    }

    private String readProbeResult(Process process) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }).get(10, TimeUnit.SECONDS);
    }

    /**
     * Tests operating-system file locks from a separate JVM without loading JavaFX or the application.
     */
    public static class LockProbe {
        /**
         * Attempts the lock and, if successful, holds it until standard input closes.
         */
        public static void main(String[] args) throws IOException {
            try (FileChannel channel = FileChannel.open(Path.of(URI.create(args[0])),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock lock = channel.tryLock()) {
                System.out.println(lock == null ? "BUSY" : "LOCKED");
                System.out.flush();
                if (lock != null) {
                    System.in.read();
                }
            }
        }
    }
}
