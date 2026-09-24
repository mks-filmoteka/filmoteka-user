package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileProvisionService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile getOrCreate(AuthUser authUser) {
        return userProfileRepository
                .findByIdentitySub(authUser.identitySub())
                .map(userProfile -> synchronizeEmail(userProfile, authUser.email()))
                .orElseGet(() -> createUserProfile(authUser));
    }

    private UserProfile createUserProfile(AuthUser authUser) {
        int inserted = userProfileRepository.insertIfAbsent(
                authUser.identitySub(),
                authUser.email(),
                authUser.displayName()
        );
        UserProfile userProfile = userProfileRepository
                .findByIdentitySub(authUser.identitySub())
                .orElseThrow(() -> new ConflictException("User profile with this email already exists"));
        if (inserted == 1) {
            log.info("Provisioned user profile id={}", userProfile.getId());
        }
        return synchronizeEmail(userProfile, authUser.email());
    }

    private UserProfile synchronizeEmail(UserProfile userProfile, String authEmail) {
        if (userProfile.getEmail().equals(authEmail)) {
            return userProfile;
        }

        if (userProfileRepository.existsByEmail(authEmail)) {
            throw new ConflictException("User profile with this email already exists");
        }

        userProfile.setEmail(authEmail);
        UserProfile saved = userProfileRepository.save(userProfile);

        log.info("Updated email for user profile id={}", saved.getId());

        return saved;
    }
}
