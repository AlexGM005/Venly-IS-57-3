package com.venly.backend.service;

import com.venly.backend.dto.CreateProfileRequest;
import com.venly.backend.dto.ProfileResponse;
import java.util.List;

public interface ProfileService {

    ProfileResponse createProfile(CreateProfileRequest request);

    ProfileResponse getProfileById(Long id);

    /** CLIENTE may only read its own profile; GERENTE and JEFE may read any profile. */
    ProfileResponse getProfile(Long pathId, Long requesterId);

    List<ProfileResponse> getProfilesForChief(Long requesterId);
}
