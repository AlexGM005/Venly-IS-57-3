package com.venly.backend.service.impl;

import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Profile;
import com.venly.backend.repository.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Resolves the simulated session (X-Perfil-Id) into a profile and enforces role rules. */
@Component
class RequesterResolver {

    private final ProfileRepository profileRepository;

    RequesterResolver(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    Profile require(Long requesterId) {
        if (requesterId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para realizar esta acción");
        }
        return profileRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED,
                        "La sesión no corresponde a un perfil existente"));
    }

    Profile requireStaff(Long requesterId, String forbiddenMessage) {
        Profile requester = require(requesterId);
        if (requester.getRol() == null || !requester.getRol().isStaff()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
        return requester;
    }
}
