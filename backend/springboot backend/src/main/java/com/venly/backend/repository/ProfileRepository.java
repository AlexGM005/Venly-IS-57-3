package com.venly.backend.repository;

import com.venly.backend.model.Profile;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository {

    Profile save(Profile profile);

    Optional<Profile> findById(Long id);

    Optional<Profile> findByCorreo(String correo);

    Optional<Profile> findByCedula(String cedula);

    List<Profile> findAll();
}
