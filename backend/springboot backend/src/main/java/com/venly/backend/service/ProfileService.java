package com.venly.backend.service;

import com.venly.backend.dto.ProfileResponse;
import com.venly.backend.dto.UpdateProfileRequest;
import java.util.List;

public interface ProfileService {

    ProfileResponse getMe(Long requesterId);

    /** Updates nombre, correo and teléfono of the requester's own profile. */
    ProfileResponse updateMe(Long requesterId, UpdateProfileRequest request);

    /** Only GERENTE and JEFE may list every profile. */
    List<ProfileResponse> getAll(Long requesterId);

    /** CLIENTE may only read its own profile; GERENTE and JEFE may read any profile. */
    ProfileResponse getById(Long id, Long requesterId);
}
