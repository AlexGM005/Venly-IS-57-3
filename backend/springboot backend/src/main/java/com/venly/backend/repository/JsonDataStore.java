package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generic, thread-safe access to the JSON data file.
 * Each repository reads/writes only its own top-level collection; other collections are preserved untouched.
 */
@Component
public class JsonDataStore {

    private final ObjectMapper objectMapper;
    private final Path dataFilePath;
    private final ReentrantLock lock = new ReentrantLock();

    public JsonDataStore(
            ObjectMapper objectMapper,
            @Value("${venly.data.file:data/Base.json}") String dataFile
    ) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        this.dataFilePath = Path.of(dataFile).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize() {
        lock.lock();
        try {
            Path parent = dataFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(dataFilePath) || Files.size(dataFilePath) == 0) {
                writeRoot(objectMapper.createObjectNode());
            } else {
                try {
                    readRoot(); // validate it parses correctly
                } catch (IOException ex) {
                    // Corrupted file — back it up and start fresh
                    Path backup = dataFilePath.resolveSibling(dataFilePath.getFileName() + ".corrupt");
                    Files.copy(dataFilePath, backup, StandardCopyOption.REPLACE_EXISTING);
                    writeRoot(objectMapper.createObjectNode());
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo inicializar el archivo de datos: " + dataFilePath, ex);
        } finally {
            lock.unlock();
        }
    }

    /** Returns a mutable copy of the collection stored under {@code key} (empty list if absent). */
    public <T> List<T> getCollection(String key, TypeReference<List<T>> type) {
        lock.lock();
        try {
            return readCollection(readRoot(), key, type);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer la colección " + key, ex);
        } finally {
            lock.unlock();
        }
    }

    /** Replaces the collection stored under {@code key}, preserving the rest of the file. */
    public <T> void saveCollection(String key, List<T> data) {
        lock.lock();
        try {
            ObjectNode root = readRoot();
            root.set(key, objectMapper.valueToTree(data));
            writeRoot(root);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la colección " + key, ex);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomic read-modify-write over a single collection: the lock is held for the whole operation,
     * so concurrent callers cannot interleave (e.g. when generating IDs).
     */
    public <T, R> R updateCollection(String key, TypeReference<List<T>> type, Function<List<T>, R> operation) {
        lock.lock();
        try {
            ObjectNode root = readRoot();
            List<T> items = readCollection(root, key, type);
            R result = operation.apply(items);
            root.set(key, objectMapper.valueToTree(items));
            writeRoot(root);
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo actualizar la colección " + key, ex);
        } finally {
            lock.unlock();
        }
    }

    /** Appends {@code item} to the collection, assigning the next incremental id atomically. */
    public <T> T insert(String key, TypeReference<List<T>> type, T item,
                        Function<T, Long> idGetter, BiConsumer<T, Long> idSetter) {
        return updateCollection(key, type, items -> {
            long nextId = items.stream()
                    .map(idGetter)
                    .filter(Objects::nonNull)
                    .max(Long::compareTo)
                    .orElse(0L) + 1;
            idSetter.accept(item, nextId);
            items.add(item);
            return item;
        });
    }

    /** Replaces the element with the same id; throws if it does not exist. */
    public <T> T replace(String key, TypeReference<List<T>> type, T item, Function<T, Long> idGetter) {
        Long id = idGetter.apply(item);
        return updateCollection(key, type, items -> {
            for (int i = 0; i < items.size(); i++) {
                if (Objects.equals(idGetter.apply(items.get(i)), id)) {
                    items.set(i, item);
                    return item;
                }
            }
            throw new IllegalStateException("No existe el elemento " + id + " en la colección " + key);
        });
    }

    private <T> List<T> readCollection(ObjectNode root, String key, TypeReference<List<T>> type) {
        JsonNode node = root.get(key);
        if (node == null || node.isNull()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(objectMapper.convertValue(node, type));
    }

    private ObjectNode readRoot() throws IOException {
        if (!Files.exists(dataFilePath) || Files.size(dataFilePath) == 0) {
            return objectMapper.createObjectNode();
        }
        JsonNode node = objectMapper.readTree(dataFilePath.toFile());
        if (!(node instanceof ObjectNode objectNode)) {
            throw new IOException("El archivo de datos no contiene un objeto JSON válido: " + dataFilePath);
        }
        return objectNode;
    }

    private void writeRoot(ObjectNode root) throws IOException {
        Path tempFile = dataFilePath.resolveSibling(dataFilePath.getFileName() + ".tmp");
        objectMapper.writeValue(tempFile.toFile(), root);
        try {
            Files.move(tempFile, dataFilePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tempFile, dataFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
