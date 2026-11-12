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
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Creación de perfil y asignación de contraseña\nDe: AuthServiceImpl.java -> Hacia: JsonProfileRepository.java");
        Role role = request.getRol() == null ? Role.CLIENTE : request.getRol();

        if (profileRepository.findByCorreo(request.getCorreo()).isPresent()) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de correo duplicado\nDe: AuthServiceImpl.java\nEstado: 409 (Conflicto - El correo ya está registrado)");
            throw new BusinessException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (profileRepository.findByCedula(request.getCedula()).isPresent()) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de cédula duplicada\nDe: AuthServiceImpl.java\nEstado: 409 (Conflicto - La cédula ya está registrada)");
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

        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Creación de perfil\nDe: AuthServiceImpl.java -> Hacia: JsonProfileRepository.java\nEstado: 201 (Creado - Perfil registrado con contraseña encriptada)");
        return ProfileMapper.toResponse(profileRepository.save(profile));
    }

    @Override
    public ProfileResponse login(LoginRequest request) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Verificación de Hash BCrypt\nDe: AuthServiceImpl.java -> Hacia: JsonProfileRepository.java");
        Profile profile = profileRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> {
                    System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Verificación de Hash BCrypt\nDe: AuthServiceImpl.java\nEstado: 401 (No Autorizado - Correo no registrado)");
                    return new BusinessException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
                });
        if (profile.getPasswordHash() == null
                || !passwordEncoder.matches(request.getContrasena(), profile.getPasswordHash())) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Verificación de Hash BCrypt\nDe: AuthServiceImpl.java\nEstado: 401 (No Autorizado - Contraseña incorrecta)");
            throw new BusinessException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Verificación de Hash BCrypt\nDe: AuthServiceImpl.java -> Hacia: JsonProfileRepository.java\nEstado: 200 (Éxito - Contraseña validada)");
        return ProfileMapper.toResponse(profile);
    }

    private void validateOrganizationKey(String organizationKey) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Validación de clave de organización (rol Staff)\nDe: AuthServiceImpl.java -> Hacia: OrganizationKeyProvider.java");
        String hash = organizationKeyProvider.getOrganizationKeyHash();
        if (hash == null || hash.isBlank()) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de clave de organización\nDe: AuthServiceImpl.java\nEstado: 403 (Prohibido - Clave de organización no configurada)");
            throw new BusinessException(HttpStatus.FORBIDDEN, "La clave de organización no está configurada");
        }
        if (organizationKey == null || organizationKey.isBlank()) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de clave de organización\nDe: AuthServiceImpl.java\nEstado: 400 (Solicitud Incorrecta - Falta la clave de organización)");
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe indicar la clave de organización para este rol");
        }
        if (!passwordEncoder.matches(organizationKey.trim(), hash)) {
            System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Validación de clave de organización\nDe: AuthServiceImpl.java\nEstado: 403 (Prohibido - Clave de organización inválida)");
            throw new BusinessException(HttpStatus.FORBIDDEN, "La clave de organización es inválida");
        }
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Validación de clave de organización\nDe: AuthServiceImpl.java\nEstado: 200 (Éxito - Clave de organización válida)");
    }
}
