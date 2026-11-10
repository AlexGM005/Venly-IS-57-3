package com.venly.backend.service;

import com.venly.backend.dto.CreateProfileRequest;
import com.venly.backend.dto.LoginRequest;
import com.venly.backend.dto.ProfileResponse;

public interface AuthService {

    /** Creates a profile (CLIENTE by default; GERENTE/JEFE require the organization key). */
    ProfileResponse register(CreateProfileRequest request);

    /** Validates e-mail and password; returns the profile or fails with 401. */
    ProfileResponse login(LoginRequest request);
}
