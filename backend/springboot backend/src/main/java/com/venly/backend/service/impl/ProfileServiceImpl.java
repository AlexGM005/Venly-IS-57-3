package com.venly.backend.service.impl;

import com.venly.backend.config.OrganizationKeyProvider;
import com.venly.backend.dto.CreateProfileRequest;
import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Profile;
import com.venly.backend.model.Role;
import com.venly.backend.repository.ProfileRepository;
import com.venly.backend.service.ProfileService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationKeyProvider organizationKeyProvider;

    public ProfileServiceImpl(
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            OrganizationKeyProvider organizationKeyProvider
    ) {
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationKeyProvider = organizationKeyProvider;
    }

    @Override
    public ProfileResponse createProfile(CreateProfileRequest request) {
        Role requestedRole = request.getRol() == null ? Role.CLIENTE : request.getRol();

        if (profileRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        if (profileRepository.findByCedula(request.getCedula()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }

        if (requestedRole == Role.GERENTE || requestedRole == Role.JEFE) {
            validateOrganizationKey(request.getClaveOrganizacion());
        }

        Profile profile = new Profile();
        profile.setNombre(request.getNombre().trim());
        profile.setCedula(request.getCedula().trim());
        profile.setCorreo(request.getCorreo().trim().toLowerCase());
        profile.setTelefono(request.getTelefono().trim());
        profile.setRol(requestedRole == null ? Role.CLIENTE : requestedRole);
        profile.setPasswordHash(passwordEncoder.encode(request.getContrasena()));

        Profile createdProfile = profileRepository.save(profile);
        return toResponse(createdProfile);
    }

    @Override
    public ProfileResponse getProfileById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        return toResponse(profile);
    }

    @Override
    public ProfileResponse getOwnProfile(Long pathId, Long requesterId) {
        if (!pathId.equals(requesterId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No tiene permisos para consultar este perfil");
        }
        return getProfileById(pathId);
    }

    @Override
    public List<ProfileResponse> getProfilesForChief(Long requesterId) {
        Profile requester = profileRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Perfil solicitante no encontrado"));

        if (requester.getRol() != Role.JEFE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Solo un jefe puede consultar el listado de perfiles");
        }

        return profileRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void validateOrganizationKey(String organizationKey) {
        String hash = organizationKeyProvider.getOrganizationKeyHash();
        if (hash == null || hash.isBlank()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "La clave de organización no está configurada");
        }
        if (organizationKey == null || organizationKey.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe indicar la clave de organización para este rol");
        }
        if (!passwordEncoder.matches(organizationKey, hash)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "La clave de organización es inválida");
        }
    }

    private ProfileResponse toResponse(Profile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setNombre(profile.getNombre());
        response.setCedula(profile.getCedula());
        response.setCorreo(profile.getCorreo());
        response.setTelefono(profile.getTelefono());
        response.setRol(profile.getRol());
        return response;
    }
}
