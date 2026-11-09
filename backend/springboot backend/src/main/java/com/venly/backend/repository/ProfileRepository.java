package com.venly.backend.repository;

import com.venly.backend.model.Profile;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository {

    /** Inserts the profile assigning a new incremental id. */
    Profile save(Profile profile);

    /** Replaces an existing profile (matched by id). */
    Profile update(Profile profile);

    Optional<Profile> findById(Long id);

    Optional<Profile> findByCorreo(String correo);

    Optional<Profile> findByCedula(String cedula);

    List<Profile> findAll();
}
