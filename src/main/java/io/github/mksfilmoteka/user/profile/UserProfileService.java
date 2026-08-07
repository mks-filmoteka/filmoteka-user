package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileProvisionService provisioningService;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileResponse getUserProfile(AuthUser authUser) {
        UserProfile userProfile = provisioningService.getOrCreate(authUser);

        return userProfileMapper.userProfileToUserProfileResponse(userProfile);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(AuthUser authUser, UserProfileRequest request) {
        UserProfile userProfile = provisioningService.getOrCreate(authUser);

        userProfileMapper.updateUserProfileRequestToUserProfile(request, userProfile);
        UserProfile saved = userProfileRepository.save(userProfile);
        log.info("Updated user profile id={} with displayName={}", saved.getId(), saved.getDisplayName());

        return userProfileMapper.userProfileToUserProfileResponse(saved);
    }
}
