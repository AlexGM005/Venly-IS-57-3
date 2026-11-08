package com.venly.backend.service;

import com.venly.backend.dto.CreateProfileRequest;
import com.venly.backend.dto.ProfileResponse;
import java.util.List;

public interface ProfileService {

    ProfileResponse createProfile(CreateProfileRequest request);

    ProfileResponse getProfileById(Long id);

    ProfileResponse getOwnProfile(Long pathId, Long requesterId);

    List<ProfileResponse> getProfilesForChief(Long requesterId);
}
