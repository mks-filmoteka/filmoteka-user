package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileProvisionService {

    private static final int MAX_DISPLAY_NAME_LENGTH = 100;

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile getOrCreate(AuthUser authUser) {
        return userProfileRepository
                .findByIdentitySub(authUser.identitySub())
                .map(userProfile -> synchronizeEmail(userProfile, authUser.email()))
                .orElseGet(() -> createUserProfile(authUser));
    }

    private UserProfile createUserProfile(AuthUser authUser) {
        String displayName = authUser.displayName();
        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            displayName = displayName.substring(0, MAX_DISPLAY_NAME_LENGTH);
        }

        int inserted = userProfileRepository.insertIfAbsent(
                authUser.identitySub(),
                authUser.email(),
                displayName
        );
        UserProfile userProfile = userProfileRepository
                .findByIdentitySub(authUser.identitySub())
                .orElseThrow(() -> new IllegalStateException("User profile was not found after provisioning"));
        if (inserted == 1) {
            log.info("Provisioned user profile id={}", userProfile.getId());
        }
        return synchronizeEmail(userProfile, authUser.email());
    }

    private UserProfile synchronizeEmail(UserProfile userProfile, String authEmail) {
        if (userProfile.getEmail().equals(authEmail)) {
            return userProfile;
        }

        userProfile.setEmail(authEmail);
        UserProfile saved = userProfileRepository.save(userProfile);

        log.info("Updated email for user profile id={}", saved.getId());

        return saved;
    }
}
