package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Profile;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
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

    /**
     * Inserta un nuevo perfil de forma atómica.
     * Verifica dentro del mismo lock que el correo y la cédula no estén duplicados.
     */
    @Override
    public Profile save(Profile profile) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Verificación atómica de duplicados e inserción de perfil\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        return dataStore.updateCollection(COLLECTION_KEY, PROFILE_LIST_TYPE, profiles -> {
            String correoNorm = normalize(profile.getCorreo());
            String cedulaNorm = normalize(profile.getCedula());

            profiles.stream()
                    .filter(p -> normalize(p.getCorreo()).equals(correoNorm))
                    .findFirst()
                    .ifPresent(p -> {
                        System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de correo duplicado\nDe: JsonProfileRepository.java\nEstado: 409 (Conflicto - El correo ya está registrado)");
                        throw new BusinessException(HttpStatus.CONFLICT, "El correo ya está registrado");
                    });

            profiles.stream()
                    .filter(p -> normalize(p.getCedula()).equals(cedulaNorm))
                    .findFirst()
                    .ifPresent(p -> {
                        System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de cédula duplicada\nDe: JsonProfileRepository.java\nEstado: 409 (Conflicto - La cédula ya está registrada)");
                        throw new BusinessException(HttpStatus.CONFLICT, "La cédula ya está registrada");
                    });

            long nextId = profiles.stream()
                    .map(Profile::getId).filter(Objects::nonNull)
                    .max(Long::compareTo).orElse(0L) + 1;
            profile.setId(nextId);
            profiles.add(profile);
            System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Inserción de nuevo perfil\nDe: JsonProfileRepository.java -> Hacia: data/Base.json\nEstado: 201 (Creado - Perfil guardado con id " + nextId + ")");
            return profile;
        });
    }

    /**
     * Actualiza un perfil existente de forma atómica.
     * Verifica dentro del mismo lock que el nuevo correo no esté en uso por otro perfil.
     */
    @Override
    public Profile update(Profile profile) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Verificación atómica de correo duplicado y actualización de perfil\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        return dataStore.updateCollection(COLLECTION_KEY, PROFILE_LIST_TYPE, profiles -> {
            String correoNorm = normalize(profile.getCorreo());

            profiles.stream()
                    .filter(p -> !Objects.equals(p.getId(), profile.getId()))
                    .filter(p -> normalize(p.getCorreo()).equals(correoNorm))
                    .findFirst()
                    .ifPresent(p -> {
                        System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de correo duplicado en actualización\nDe: JsonProfileRepository.java\nEstado: 409 (Conflicto - El correo ya está en uso)");
                        throw new BusinessException(HttpStatus.CONFLICT, "El correo ya está en uso por otro perfil");
                    });

            for (int i = 0; i < profiles.size(); i++) {
                if (Objects.equals(profiles.get(i).getId(), profile.getId())) {
                    profiles.set(i, profile);
                    System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Actualización de perfil\nDe: JsonProfileRepository.java -> Hacia: data/Base.json\nEstado: 200 (Éxito - Perfil " + profile.getId() + " actualizado)");
                    return profile;
                }
            }
            throw new IllegalStateException("No existe el perfil " + profile.getId() + " en la colección");
        });
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
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de perfil por correo (credenciales)\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
        String target = normalize(correo);
        return findAll().stream().filter(p -> normalize(p.getCorreo()).equals(target)).findFirst();
    }

    @Override
    public Optional<Profile> findByCedula(String cedula) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de perfil por cédula\nDe: JsonProfileRepository.java -> Hacia: data/Base.json");
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
