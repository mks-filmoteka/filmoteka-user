package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.config.RepositoryTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(RepositoryTestConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void shouldSaveAndLoadUserProfile() {
        UserProfile savedProfile = userProfileRepository.saveAndFlush(userProfile());
        Optional<UserProfile> loadedProfile = userProfileRepository.findById(savedProfile.getId());

        assertNotNull(savedProfile.getId());
        assertTrue(loadedProfile.isPresent());
        assertEquals(IDENTITY_SUB, loadedProfile.get().getIdentitySub());
        assertEquals(EMAIL, loadedProfile.get().getEmail());
        assertEquals(DISPLAY_NAME, loadedProfile.get().getDisplayName());
        assertNotNull(loadedProfile.get().getCreatedTs());
        assertNotNull(loadedProfile.get().getUpdatedTs());
    }

    @Test
    void shouldFindUserProfileByIdentitySub() {
        UserProfile savedProfile = userProfileRepository.saveAndFlush(userProfile());

        Optional<UserProfile> loadedProfile = userProfileRepository.findByIdentitySub(savedProfile.getIdentitySub());

        assertNotNull(savedProfile.getId());
        assertTrue(loadedProfile.isPresent());
        assertEquals(IDENTITY_SUB, loadedProfile.get().getIdentitySub());
    }

    @Test
    void shouldFindUserProfileByEmail() {
        UserProfile savedProfile = userProfileRepository.saveAndFlush(userProfile());

        Optional<UserProfile> loadedProfile = userProfileRepository.findByEmail(savedProfile.getEmail());

        assertNotNull(savedProfile.getId());
        assertTrue(loadedProfile.isPresent());
        assertEquals(EMAIL, loadedProfile.get().getEmail());
    }

    @Test
    void shouldCheckIfUserProfileExistsByIdentitySub() {
        userProfileRepository.saveAndFlush(userProfile());

        boolean exists = userProfileRepository.existsByIdentitySub(IDENTITY_SUB);

        assertTrue(exists);
    }

    @Test
    void shouldCheckIfUserProfileExistsByEmail() {
        userProfileRepository.saveAndFlush(userProfile());

        boolean exists = userProfileRepository.existsByEmail(EMAIL);

        assertTrue(exists);
    }

    @Test
    void shouldThrowOnDuplicateIdentitySubConflict() {
        userProfileRepository.saveAndFlush(userProfile());

        UserProfile duplicate = UserProfileTestData.userProfile(IDENTITY_SUB, "other@gmail.com");

        assertThrows(DataIntegrityViolationException.class, () -> userProfileRepository.saveAndFlush(duplicate));
    }

    @Test
    void shouldThrowOnDuplicateEmailConflict() {
        userProfileRepository.saveAndFlush(userProfile());

        UserProfile duplicate = UserProfileTestData.userProfile("other-sub", EMAIL);

        assertThrows(DataIntegrityViolationException.class, () -> userProfileRepository.saveAndFlush(duplicate));
    }
}
