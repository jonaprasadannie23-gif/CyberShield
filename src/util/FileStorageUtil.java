package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic file-storage utility using Java object serialisation.
 *
 * Each entity type is stored in its own file under the application's data
 * directory.  Corrupted or missing files are handled gracefully so the
 * application always starts cleanly.
 *
 * OOP – Abstraction / generics: one utility handles any Serializable type.
 */
public final class FileStorageUtil {

    /** Base directory for data files – resolved relative to the JVM working directory. */
    private static final String DATA_DIR = "data";

    private FileStorageUtil() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Persists a list of objects to a binary file.
     *
     * @param filename  filename inside the data directory (e.g. "users.dat")
     * @param objects   list of Serializable objects to write
     */
    public static <T extends Serializable> void saveAll(String filename, List<T> objects)
            throws IOException {
        ensureDataDir();
        Path path = Paths.get(DATA_DIR, filename);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(objects);
        }
    }

    /**
     * Loads a list of objects from a binary file.
     * Returns an empty list if the file does not exist or cannot be read.
     *
     * @param filename  filename inside the data directory
     * @param <T>       expected element type
     * @return          mutable list of loaded objects
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> loadAll(String filename) {
        Path path = Paths.get(DATA_DIR, filename);
        if (!Files.exists(path)) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(path)))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                return (List<T>) obj;
            }
        } catch (InvalidClassException e) {
            System.err.println("[FileStorageUtil] Incompatible serialised class in '"
                               + filename + "' – resetting file. " + e.getMessage());
            try { Files.delete(path); } catch (IOException ignored) {}
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileStorageUtil] Could not read '" + filename
                               + "': " + e.getMessage() + " – starting with empty list.");
        }
        return new ArrayList<>();
    }

    /**
     * Deletes a data file.  Used for the demo-data reset feature.
     */
    public static void deleteFile(String filename) {
        try {
            Files.deleteIfExists(Paths.get(DATA_DIR, filename));
        } catch (IOException e) {
            System.err.println("[FileStorageUtil] Could not delete '" + filename + "': " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private static void ensureDataDir() throws IOException {
        Path dir = Paths.get(DATA_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}
