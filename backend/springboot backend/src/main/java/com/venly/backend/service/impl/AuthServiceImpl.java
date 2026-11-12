package com.venly.backend.service.impl;

import com.venly.backend.config.OrganizationKeyProvider;
import com.venly.backend.dto.CreateProfileRequest;
import com.venly.backend.dto.LoginRequest;
import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Profile;
import com.venly.backend.model.Role;
import com.venly.backend.repository.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.venly.backend.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String INVALID_CREDENTIALS = "Correo o contraseña incorrectos";

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationKeyProvider organizationKeyProvider;

    public AuthServiceImpl(ProfileRepository profileRepository,
                           PasswordEncoder passwordEncoder,
                           OrganizationKeyProvider organizationKeyProvider) {
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationKeyProvider = organizationKeyProvider;
    }

    @Override
    public ProfileResponse register(CreateProfileRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Validando duplicados (correo/cédula) y asignando rol y contraseña con hash BCrypt en backend/springboot backend/src/main/java/com/venly/backend/service/impl/AuthServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java");
        Role role = request.getRol() == null ? Role.CLIENTE : request.getRol();

        if (profileRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (profileRepository.findByCedula(request.getCedula()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }
        if (role.isStaff()) {
            validateOrganizationKey(request.getClaveOrganizacion());
        }

        Profile profile = new Profile();
        profile.setNombre(ProfileMapper.normalizeName(request.getNombre()));
        profile.setCedula(request.getCedula().trim());
        profile.setCorreo(request.getCorreo().trim().toLowerCase());
        profile.setTelefono(request.getTelefono().trim());
        profile.setRol(role);
        profile.setPasswordHash(passwordEncoder.encode(request.getContrasena()));

        return ProfileMapper.toResponse(profileRepository.save(profile));
    }

    @Override
    public ProfileResponse login(LoginRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Verificando credenciales y comparando hash BCrypt en backend/springboot backend/src/main/java/com/venly/backend/service/impl/AuthServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonProfileRepository.java");
        Profile profile = profileRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS));
        if (profile.getPasswordHash() == null
                || !passwordEncoder.matches(request.getContrasena(), profile.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        return ProfileMapper.toResponse(profile);
    }

    private void validateOrganizationKey(String organizationKey) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Validando clave de organización para creación de perfil Staff (Gerente/Jefe) en backend/springboot backend/src/main/java/com/venly/backend/service/impl/AuthServiceImpl.java -> consultando backend/springboot backend/src/main/java/com/venly/backend/config/OrganizationKeyProvider.java");
        String hash = organizationKeyProvider.getOrganizationKeyHash();
        if (hash == null || hash.isBlank()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "La clave de organización no está configurada");
        }
        if (organizationKey == null || organizationKey.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe indicar la clave de organización para este rol");
        }
        if (!passwordEncoder.matches(organizationKey.trim(), hash)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "La clave de organización es inválida");
        }
    }
}
