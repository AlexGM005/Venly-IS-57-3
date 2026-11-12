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
        System.out.println("[VENLY TRACE - BACKEND] Acción: Resolviendo sesión y consultando perfil propio en backend/springboot backend/src/main/java/com/venly/backend/service/impl/ProfileServiceImpl.java -> consultando backend/springboot backend/src/main/java/com/venly/backend/service/impl/RequesterResolver.java");
        return ProfileMapper.toResponse(requesterResolver.require(requesterId));
    }

    @Override
    public ProfileResponse updateMe(Long requesterId, UpdateProfileRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Validando correo duplicado y actualizando perfil propio en backend/springboot backend/src/main/java/com/venly/backend/service/impl/ProfileServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java");
        Profile profile = requesterResolver.require(requesterId);
        String newEmail = request.getCorreo().trim().toLowerCase();

        profileRepository.findByCorreo(newEmail)
                .filter(other -> !Objects.equals(other.getId(), profile.getId()))
                .ifPresent(other -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "El correo ya está en uso por otro perfil");
                });

        profile.setNombre(ProfileMapper.normalizeName(request.getNombre()));
        profile.setCorreo(newEmail);
        profile.setTelefono(request.getTelefono().trim());
        return ProfileMapper.toResponse(profileRepository.update(profile));
    }

    @Override
    public List<ProfileResponse> getAll(Long requesterId) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Verificando restricción de Staff y listando todos los perfiles en backend/springboot backend/src/main/java/com/venly/backend/service/impl/ProfileServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java");
        requesterResolver.requireStaff(requesterId, "Solo un gerente o jefe puede consultar el listado de perfiles");
        return profileRepository.findAll().stream()
                .sorted(Comparator.comparing(Profile::getId))
                .map(ProfileMapper::toResponse)
                .toList();
    }

    @Override
    public ProfileResponse getById(Long id, Long requesterId) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Verificando restricción Cliente vs Staff para consultar perfil por id en backend/springboot backend/src/main/java/com/venly/backend/service/impl/ProfileServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java");
        Profile requester = requesterResolver.require(requesterId);
        boolean isOwnProfile = Objects.equals(requester.getId(), id);
        if (!isOwnProfile && (requester.getRol() == null || !requester.getRol().isStaff())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Un cliente solo puede consultar su propio perfil");
        }
        return profileRepository.findById(id)
                .map(ProfileMapper::toResponse)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
    }
}
