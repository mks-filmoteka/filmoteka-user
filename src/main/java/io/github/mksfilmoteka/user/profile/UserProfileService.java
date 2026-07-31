package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileResponse findById(Long id) {
        UserProfile userProfile = getUserProfileOrThrow(id);
        return userProfileMapper.userProfileToUserProfileResponse(userProfile);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long id, UserProfileRequest request) {
        UserProfile userProfile = getUserProfileOrThrow(id);

        userProfileMapper.updateUserProfileRequestToUserProfile(request, userProfile);
        UserProfile saved = userProfileRepository.save(userProfile);

        return userProfileMapper.userProfileToUserProfileResponse(saved);
    }

    private UserProfile getUserProfileOrThrow(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User profile with id " + id + " not found"));
    }
}