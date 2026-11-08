package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.venly.backend.model.Profile;
import com.venly.backend.model.Room;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class JsonProfileRepository implements ProfileRepository {

    private static final TypeReference<List<Profile>> PROFILE_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final ReentrantLock lock = new ReentrantLock();
    private final Path dataFilePath;

    public JsonProfileRepository(
            ObjectMapper objectMapper,
            @Value("${venly.data.file:../../data/Base.json}") String dataFile
    ) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        this.dataFilePath = Path.of(dataFile).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initialize() {
        lock.lock();
        try {
            if (!Files.exists(dataFilePath.getParent())) {
                Files.createDirectories(dataFilePath.getParent());
            }
            if (!Files.exists(dataFilePath)) {
                DatabaseContent initialData = new DatabaseContent();
                writeAll(initialData);
                return;
            }
            DatabaseContent content = readAll();
            writeAll(content);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo inicializar el archivo de datos", ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Profile save(Profile profile) {
        lock.lock();
        try {
            DatabaseContent content = readAll();
            long nextId = content.perfiles().stream()
                    .map(Profile::getId)
                    .filter(id -> id != null)
                    .max(Long::compareTo)
                    .orElse(0L) + 1;
            profile.setId(nextId);
            content.perfiles().add(profile);
            writeAll(content);
            return profile;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el perfil", ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Profile> findById(Long id) {
        lock.lock();
        try {
            return readAll().perfiles().stream()
                    .filter(profile -> profile.getId() != null && profile.getId().equals(id))
                    .findFirst();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo consultar el perfil", ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Profile> findByCorreo(String correo) {
        String normalizedEmail = normalize(correo);
        lock.lock();
        try {
            return readAll().perfiles().stream()
                    .filter(profile -> normalize(profile.getCorreo()).equals(normalizedEmail))
                    .findFirst();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo consultar el correo", ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Profile> findByCedula(String cedula) {
        String normalizedCedula = normalize(cedula);
        lock.lock();
        try {
            return readAll().perfiles().stream()
                    .filter(profile -> normalize(profile.getCedula()).equals(normalizedCedula))
                    .findFirst();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo consultar la cédula", ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Profile> findAll() {
        lock.lock();
        try {
            return new ArrayList<>(readAll().perfiles());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo consultar los perfiles", ex);
        } finally {
            lock.unlock();
        }
    }

    private DatabaseContent readAll() throws IOException {
        if (!Files.exists(dataFilePath) || Files.size(dataFilePath) == 0) {
            return new DatabaseContent();
        }

        Map<String, Object> root = objectMapper.readValue(dataFilePath.toFile(), new TypeReference<>() {
        });

        Object peliculasRaw = root.get("Peliculas");
        Object perfilesRaw = root.get("Perfiles");
        Object salasRaw = root.get("Salas");

        List<Map<String, Object>> peliculas = castListMap(peliculasRaw);
        List<Profile> perfiles = perfilesRaw == null
                ? new ArrayList<>()
                : objectMapper.convertValue(perfilesRaw, PROFILE_LIST_TYPE);
        List<Room> salas = salasRaw == null
                ? createDefaultRooms()
                : objectMapper.convertValue(salasRaw, new TypeReference<List<Room>>() {
                });

        return new DatabaseContent(peliculas, perfiles, salas);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castListMap(Object raw) {
        if (raw == null) {
            return new ArrayList<>();
        }
        return objectMapper.convertValue(raw, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private void writeAll(DatabaseContent content) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("Peliculas", objectMapper.valueToTree(content.peliculas()));
        root.set("Perfiles", objectMapper.valueToTree(content.perfiles()));
        root.set("Salas", objectMapper.valueToTree(content.salas()));

        Path tempFile = dataFilePath.resolveSibling(dataFilePath.getFileName() + ".tmp");
        objectMapper.writeValue(tempFile.toFile(), root);
        try {
            Files.move(tempFile, dataFilePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException atomicMoveException) {
            Files.move(tempFile, dataFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private List<Room> createDefaultRooms() {
        List<Room> rooms = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            rooms.add(new Room(i, "Sala " + i, 120));
        }
        return rooms;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private record DatabaseContent(
            List<Map<String, Object>> peliculas,
            List<Profile> perfiles,
            List<Room> salas
    ) {
        private DatabaseContent() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        private DatabaseContent(List<Map<String, Object>> peliculas, List<Profile> perfiles, List<Room> salas) {
            this.peliculas = peliculas == null ? new ArrayList<>() : peliculas;
            this.perfiles = perfiles == null ? new ArrayList<>() : perfiles;
            this.salas = (salas == null || salas.isEmpty()) ? defaultRooms() : salas;
        }

        private static List<Room> defaultRooms() {
            List<Room> rooms = new ArrayList<>();
            for (long i = 1; i <= 5; i++) {
                rooms.add(new Room(i, "Sala " + i, 120));
            }
            return rooms;
        }
    }
}
