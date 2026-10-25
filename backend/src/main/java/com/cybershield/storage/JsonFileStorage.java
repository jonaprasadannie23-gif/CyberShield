package com.cybershield.storage;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON file persistence that survives application restarts.
 * Corrupted or missing files yield an empty list so the app always starts.
 */
@Component
public class JsonFileStorage {

    private static final Logger log = LoggerFactory.getLogger(JsonFileStorage.class);

    private final Path dataDir;
    private final ObjectMapper mapper;

    public JsonFileStorage(@Value("${cybershield.data.dir:data}") String dataDir,
                           ObjectMapper objectMapper) {
        this.dataDir = Path.of(dataDir);
        this.mapper = objectMapper.copy();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public synchronized <T> List<T> loadAll(String filename, Class<T> type) {
        Path path = dataDir.resolve(filename);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            JavaType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            List<T> loaded = mapper.readValue(path.toFile(), listType);
            return loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            log.warn("Could not read '{}': {} – starting with an empty list.", filename, e.getMessage());
            return new ArrayList<>();
        }
    }

    public synchronized <T> void saveAll(String filename, List<T> objects) {
        try {
            Files.createDirectories(dataDir);
            mapper.writeValue(dataDir.resolve(filename).toFile(), objects);
        } catch (IOException e) {
            log.error("Failed to save '{}': {}", filename, e.getMessage());
            throw new IllegalStateException("Could not persist data to " + filename, e);
        }
    }

    /**
     * Saves a list of objects, declaring {@code elementClass} as the collection element type
     * so Jackson activates {@code @JsonTypeInfo} polymorphic type discriminators on subclasses.
     * Use this overload for any list whose elements may be polymorphic (e.g. {@code Incident}).
     */
    public synchronized <T> void saveAll(String filename, List<T> objects, Class<T> elementClass) {
        try {
            Files.createDirectories(dataDir);
            JavaType listType = mapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, elementClass);
            mapper.writerFor(listType).writeValue(dataDir.resolve(filename).toFile(), objects);
        } catch (IOException e) {
            log.error("Failed to save '{}': {}", filename, e.getMessage());
            throw new IllegalStateException("Could not persist data to " + filename, e);
        }
    }

    public Path getDataDir() {
        return dataDir;
    }
}
