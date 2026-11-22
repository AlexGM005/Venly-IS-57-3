package com.venly.backend.service.impl;

import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.dto.UpdateProfileRequest;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Profile;
import com.venly.backend.repository.ProfileRepository;
import com.venly.backend.service.ProfileService;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final RequesterResolver requesterResolver;

    public ProfileServiceImpl(ProfileRepository profileRepository, RequesterResolver requesterResolver) {
        this.profileRepository = profileRepository;
        this.requesterResolver = requesterResolver;
    }

    @Override
    public ProfileResponse getMe(Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de perfil propio\nDe: ProfileServiceImpl.java -> Hacia: RequesterResolver.java");
        return ProfileMapper.toResponse(requesterResolver.require(requesterId));
    }

    @Override
    public ProfileResponse updateMe(Long requesterId, UpdateProfileRequest request) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Actualización de perfil propio\nDe: ProfileServiceImpl.java -> Hacia: JsonProfileRepository.java");
        Profile profile = requesterResolver.require(requesterId);
        profile.setNombre(ProfileMapper.normalizeName(request.getNombre()));
        profile.setCorreo(request.getCorreo().trim().toLowerCase());
        profile.setTelefono(request.getTelefono().trim());
        // La verificación de correo duplicado (excluyendo el propio perfil) se
        // realiza de forma atómica dentro de JsonProfileRepository.update().
        return ProfileMapper.toResponse(profileRepository.update(profile));
    }

    @Override
    public List<ProfileResponse> getAll(Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Listado de perfiles (solo Staff)\nDe: ProfileServiceImpl.java -> Hacia: JsonProfileRepository.java");
        requesterResolver.requireStaff(requesterId, "Solo un gerente o jefe puede consultar el listado de perfiles");
        return profileRepository.findAll().stream()
                .sorted(Comparator.comparing(Profile::getId))
                .map(ProfileMapper::toResponse)
                .toList();
    }

    @Override
    public ProfileResponse getById(Long id, Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Verificación de restricción Cliente vs Staff\nDe: ProfileServiceImpl.java -> Hacia: JsonProfileRepository.java");
        Profile requester = requesterResolver.require(requesterId);
        boolean isOwnProfile = Objects.equals(requester.getId(), id);
        if (!isOwnProfile && (requester.getRol() == null || !requester.getRol().isStaff())) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Verificación de restricción Cliente vs Staff\nDe: ProfileServiceImpl.java\nEstado: 403 (Prohibido - Rol sin permisos)");
            throw new BusinessException(HttpStatus.FORBIDDEN, "Un cliente solo puede consultar su propio perfil");
        }
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Verificación de restricción Cliente vs Staff\nDe: ProfileServiceImpl.java -> Hacia: JsonProfileRepository.java\nEstado: 200 (Éxito - Acceso autorizado)");
        return profileRepository.findById(id)
                .map(ProfileMapper::toResponse)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
    }
}
