package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.venly.backend.model.Profile;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        System.out.println("[VENLY TRACE - BACKEND] Acción: Insertando nuevo perfil (Cliente/Gerente/Jefe) desde backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java -> hacia base de datos data/Base.json");
        return dataStore.insert(COLLECTION_KEY, PROFILE_LIST_TYPE, profile, Profile::getId, Profile::setId);
    }

    @Override
    public Profile update(Profile profile) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Actualizando perfil existente desde backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java -> hacia base de datos data/Base.json");
        return dataStore.replace(COLLECTION_KEY, PROFILE_LIST_TYPE, profile, Profile::getId);
    }

    @Override
    public Optional<Profile> findById(Long id) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Obteniendo perfil por id desde backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java -> hacia base de datos data/Base.json");
        if (id == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(p -> Objects.equals(p.getId(), id)).findFirst();
    }

    @Override
    public Optional<Profile> findByCorreo(String correo) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Obteniendo perfil por correo (verificación de credenciales/duplicados) desde backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java -> hacia base de datos data/Base.json");
        String target = normalize(correo);
        return findAll().stream().filter(p -> normalize(p.getCorreo()).equals(target)).findFirst();
    }

    @Override
    public Optional<Profile> findByCedula(String cedula) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Obteniendo perfil por cédula (verificación de duplicados) desde backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java -> hacia base de datos data/Base.json");
        String target = normalize(cedula);
        return findAll().stream().filter(p -> normalize(p.getCedula()).equals(target)).findFirst();
    }

    @Override
    public List<Profile> findAll() {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Obteniendo listado completo de perfiles desde backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java -> hacia base de datos data/Base.json");
        return dataStore.getCollection(COLLECTION_KEY, PROFILE_LIST_TYPE);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
