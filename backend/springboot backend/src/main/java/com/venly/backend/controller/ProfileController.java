package com.venly.backend.controller;

import com.venly.backend.dto.CreateProfileRequest;
import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.service.ProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> create(@Valid @RequestBody CreateProfileRequest request) {
        ProfileResponse created = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getOwnProfile(
            @PathVariable Long id,
            @RequestHeader("X-Perfil-Id") Long requesterId
    ) {
        return ResponseEntity.ok(profileService.getOwnProfile(id, requesterId));
    }

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getAll(
            @RequestHeader("X-Perfil-Id") Long requesterId
    ) {
        return ResponseEntity.ok(profileService.getProfilesForChief(requesterId));
    }
}
