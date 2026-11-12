package com.venly.backend.controller;

import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.dto.UpdateProfileRequest;
import com.venly.backend.service.ProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfiles")
public class ProfileController {

    static final String SESSION_HEADER = "X-Perfil-Id";

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMe(@RequestHeader(SESSION_HEADER) Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición GET /api/perfiles/me\nDe: ProfileController.java -> Hacia: ProfileServiceImpl.java");
        return ResponseEntity.ok(profileService.getMe(requesterId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMe(@RequestHeader(SESSION_HEADER) Long requesterId,
                                                    @Valid @RequestBody UpdateProfileRequest request) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición PUT /api/perfiles/me\nDe: ProfileController.java -> Hacia: ProfileServiceImpl.java");
        return ResponseEntity.ok(profileService.updateMe(requesterId, request));
    }

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getAll(@RequestHeader(SESSION_HEADER) Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición GET /api/perfiles (solo Staff)\nDe: ProfileController.java -> Hacia: ProfileServiceImpl.java");
        return ResponseEntity.ok(profileService.getAll(requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getById(@PathVariable Long id,
                                                   @RequestHeader(SESSION_HEADER) Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición GET /api/perfiles/{id} (Cliente vs Staff)\nDe: ProfileController.java -> Hacia: ProfileServiceImpl.java");
        return ResponseEntity.ok(profileService.getById(id, requesterId));
    }
}
