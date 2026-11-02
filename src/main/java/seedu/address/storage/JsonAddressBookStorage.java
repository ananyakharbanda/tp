package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    public static final String MESSAGE_UNREADABLE_FILE = "Unable to load student data.";
    public static final String MESSAGE_INVALID_FILE =
            "Unable to load student data. The data file may be corrupted or invalid.";

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);
    // Serialize access within this JVM, including across storage instances, before opening lock-file channels.
    private static final Object FILE_ACCESS_LOCK = new Object();

    private final Path filePath;
    private final Map<Path, byte[]> savedContents = new HashMap<>();

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);
        synchronized (FILE_ACCESS_LOCK) {
            return readAndRememberAddressBook(filePath.toAbsolutePath().normalize());
        }
    }

    private Optional<ReadOnlyAddressBook> readAndRememberAddressBook(Path filePath) throws DataLoadingException {
        byte[] contents;
        try {
            contents = readFileIfExists(filePath);
        } catch (IOException e) {
            throw new DataLoadingException(MESSAGE_UNREADABLE_FILE, e);
        }
        if (contents == null) {
            savedContents.put(filePath, null);
            return Optional.empty();
        }

        try {
            // Parse the same bytes remembered for conflict detection, without reading the file a second time.
            JsonSerializableAddressBook jsonAddressBook = JsonUtil.fromJsonString(
                    new String(contents, StandardCharsets.UTF_8), JsonSerializableAddressBook.class);
            if (jsonAddressBook == null) {
                throw new IllegalValueException("JSON document must not be null.");
            }
            ReadOnlyAddressBook addressBook = jsonAddressBook.toModelType();
            savedContents.put(filePath, contents);
            return Optional.of(addressBook);
        } catch (DuplicateStudentIdException dsie) {
            logger.info("Duplicate student IDs found in " + filePath + ".");
            throw new DataLoadingException(dsie.getMessage(), dsie);
        } catch (IllegalValueException | IOException e) {
            logger.info("Invalid student data found in " + filePath + ".");
            throw new DataLoadingException(MESSAGE_INVALID_FILE, e);
        }
    }

    private byte[] readFileIfExists(Path filePath) throws IOException {
        try {
            return Files.readAllBytes(filePath);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     * Writes to a temporary file in the same directory, then atomically replaces the destination.
     * If writing or replacement fails, the previous data file remains unchanged.
     * An existing file must have been read or saved by this storage instance and remain unchanged.
     * A separate lock file protects the comparison and replacement across application processes.
     *
     * @param addressBook The non-null student records to save.
     * @param filePath location of the data. Cannot be null.
     * @throws DataStorageLocationException if the folder for the data file cannot be created.
     * @throws DataFileChangedException if the file differs from the last successful read or save.
     * @throws DataFileInUseException if another process is saving to the same file.
     * @throws IOException if writing fails or the filesystem does not support atomic replacement.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        synchronized (FILE_ACCESS_LOCK) {
            saveWithFileLock(addressBook, filePath.toAbsolutePath().normalize());
        }
    }

    private void saveWithFileLock(ReadOnlyAddressBook addressBook, Path targetFile) throws IOException {
        createStorageFolder(targetFile.getParent());
        Path lockFile = targetFile.resolveSibling(targetFile.getFileName() + ".lock");
        // Keep this file after releasing the lock: deleting it could let writers lock different file identities.
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.tryLock()) {
            if (lock == null) {
                throw new DataFileInUseException();
            }
            Path temporaryFile = Files.createTempFile(targetFile.getParent(), "teachassist-", ".tmp");
            try {
                JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), temporaryFile);
                byte[] updatedContents = Files.readAllBytes(temporaryFile);
                if (!Arrays.equals(savedContents.get(targetFile), readFileIfExists(targetFile))) {
                    throw new DataFileChangedException();
                }
                replaceDataFile(temporaryFile, targetFile);
                savedContents.put(targetFile, updatedContents);
            } finally {
                removeTemporaryFile(temporaryFile);
            }
        } catch (OverlappingFileLockException e) {
            throw new DataFileInUseException();
        }
    }

    private void createStorageFolder(Path folder) throws DataStorageLocationException {
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new DataStorageLocationException("Unable to create folder " + folder, e);
        }
    }

    /**
     * Atomically replaces the saved data with a completely written temporary file.
     *
     * @param temporaryFile The completed temporary file in the destination directory.
     * @param targetFile The destination file.
     * @throws IOException if atomic replacement fails, leaving the previous destination unchanged.
     */
    protected void replaceDataFile(Path temporaryFile, Path targetFile) throws IOException {
        Files.move(temporaryFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private void removeTemporaryFile(Path temporaryFile) {
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException e) {
            logger.warning("Unable to remove temporary data file " + temporaryFile
                    + ": " + e.getClass().getSimpleName());
        }
    }

}
