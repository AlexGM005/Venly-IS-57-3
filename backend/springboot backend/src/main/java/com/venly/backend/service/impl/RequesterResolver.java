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
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Resolución de sesión simulada (X-Perfil-Id)\nDe: RequesterResolver.java -> Hacia: JsonProfileRepository.java");
        if (requesterId == null) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Resolución de sesión simulada\nDe: RequesterResolver.java\nEstado: 401 (No Autorizado - No hay sesión activa)");
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para realizar esta acción");
        }
        return profileRepository.findById(requesterId)
                .orElseThrow(() -> {
                    System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Resolución de sesión simulada\nDe: RequesterResolver.java\nEstado: 401 (No Autorizado - El perfil de la sesión no existe)");
                    return new BusinessException(HttpStatus.UNAUTHORIZED,
                        "La sesión no corresponde a un perfil existente");
                });
    }

    Profile requireStaff(Long requesterId, String forbiddenMessage) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Verificación de rol Staff (Gerente/Jefe)\nDe: RequesterResolver.java -> Hacia: Role.java");
        Profile requester = require(requesterId);
        if (requester.getRol() == null || !requester.getRol().isStaff()) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Verificación de rol Staff\nDe: RequesterResolver.java\nEstado: 403 (Prohibido - Rol sin permisos)");
            throw new BusinessException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Verificación de rol Staff\nDe: RequesterResolver.java\nEstado: 200 (Éxito - Rol autorizado)");
        return requester;
    }
}
