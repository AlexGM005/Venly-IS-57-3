package com.venly.backend.service.impl;

import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.model.Profile;

/** Converts profiles to their public representation (never exposes the password hash). */
final class ProfileMapper {

    private ProfileMapper() {
    }

    static ProfileResponse toResponse(Profile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setNombre(profile.getNombre());
        response.setCedula(profile.getCedula());
        response.setCorreo(profile.getCorreo());
        response.setTelefono(profile.getTelefono());
        response.setRol(profile.getRol());
        return response;
    }

    /** Trims and collapses repeated spaces ("  Ana   Pérez " -> "Ana Pérez"). */
    static String normalizeName(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }
}
