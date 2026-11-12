package com.venly.backend.controller;

import com.venly.backend.dto.CreateProfileRequest;
import com.venly.backend.dto.LoginRequest;
import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(@Valid @RequestBody CreateProfileRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Recibiendo petición POST /api/auth/register en backend/springboot backend/src/main/java/com/venly/backend/controller/AuthController.java -> delegando a backend/springboot backend/src/main/java/com/venly/backend/service/impl/AuthServiceImpl.java");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ProfileResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Recibiendo petición POST /api/auth/login en backend/springboot backend/src/main/java/com/venly/backend/controller/AuthController.java -> delegando a backend/springboot backend/src/main/java/com/venly/backend/service/impl/AuthServiceImpl.java");
        return ResponseEntity.ok(authService.login(request));
    }
}
