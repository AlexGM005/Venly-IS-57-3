package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.venly.backend.model.Profile;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/** Profile persistence backed by the "Perfiles" collection of the JSON data store. */
@Repository
public class JsonProfileRepository implements ProfileRepository {

    private static final String COLLECTION_KEY = "Perfiles";
    private static final TypeReference<List<Profile>> PROFILE_LIST_TYPE = new TypeReference<>() {
    };

    private final JsonDataStore dataStore;

    public JsonProfileRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Profile save(Profile profile) {
        return dataStore.updateCollection(COLLECTION_KEY, PROFILE_LIST_TYPE, profiles -> {
            long nextId = profiles.stream()
                    .map(Profile::getId)
                    .filter(Objects::nonNull)
                    .max(Long::compareTo)
                    .orElse(0L) + 1;
            profile.setId(nextId);
            profiles.add(profile);
            return profile;
        });
    }

    @Override
    public Optional<Profile> findById(Long id) {
        return findFirstBy(Profile::getId, id);
    }

    @Override
    public Optional<Profile> findByCorreo(String correo) {
        String target = normalize(correo);
        return findAll().stream().filter(p -> normalize(p.getCorreo()).equals(target)).findFirst();
    }

    @Override
    public Optional<Profile> findByCedula(String cedula) {
        String target = normalize(cedula);
        return findAll().stream().filter(p -> normalize(p.getCedula()).equals(target)).findFirst();
    }

    @Override
    public List<Profile> findAll() {
        return dataStore.getCollection(COLLECTION_KEY, PROFILE_LIST_TYPE);
    }

    private <V> Optional<Profile> findFirstBy(Function<Profile, V> getter, V value) {
        if (value == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(p -> value.equals(getter.apply(p))).findFirst();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
