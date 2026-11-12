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
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Inserción de nuevo perfil (Cliente/Gerente/Jefe)\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        return dataStore.insert(COLLECTION_KEY, PROFILE_LIST_TYPE, profile, Profile::getId, Profile::setId);
    }

    @Override
    public Profile update(Profile profile) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Actualización de perfil existente\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        return dataStore.replace(COLLECTION_KEY, PROFILE_LIST_TYPE, profile, Profile::getId);
    }

    @Override
    public Optional<Profile> findById(Long id) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de perfil por id\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        if (id == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(p -> Objects.equals(p.getId(), id)).findFirst();
    }

    @Override
    public Optional<Profile> findByCorreo(String correo) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de perfil por correo (credenciales/duplicados)\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        String target = normalize(correo);
        return findAll().stream().filter(p -> normalize(p.getCorreo()).equals(target)).findFirst();
    }

    @Override
    public Optional<Profile> findByCedula(String cedula) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de perfil por cédula (duplicados)\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        String target = normalize(cedula);
        return findAll().stream().filter(p -> normalize(p.getCedula()).equals(target)).findFirst();
    }

    @Override
    public List<Profile> findAll() {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Listado completo de perfiles\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        return dataStore.getCollection(COLLECTION_KEY, PROFILE_LIST_TYPE);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
