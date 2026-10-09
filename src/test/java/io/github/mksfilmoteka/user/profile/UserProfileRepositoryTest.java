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
    void shouldCheckIfUserProfileExistsByIdentitySub() {
        userProfileRepository.saveAndFlush(userProfile());

        boolean exists = userProfileRepository.existsByIdentitySub(IDENTITY_SUB);

        assertTrue(exists);
    }

    @Test
    void shouldInsertUserProfileIfAbsent() {
        int inserted = userProfileRepository.insertIfAbsent(IDENTITY_SUB, EMAIL, DISPLAY_NAME);

        UserProfile loadedProfile = userProfileRepository.findByIdentitySub(IDENTITY_SUB).orElseThrow();

        assertEquals(1, inserted);
        assertNotNull(loadedProfile.getId());
        assertEquals(EMAIL, loadedProfile.getEmail());
        assertEquals(DISPLAY_NAME, loadedProfile.getDisplayName());
        assertNotNull(loadedProfile.getCreatedTs());
        assertNotNull(loadedProfile.getUpdatedTs());
        assertEquals(1, userProfileRepository.count());
    }

    @Test
    void shouldIgnoreInsertWhenProfileAlreadyExists() {
        UserProfile savedProfile = userProfileRepository.saveAndFlush(userProfile());

        int inserted = userProfileRepository.insertIfAbsent(IDENTITY_SUB, NEW_EMAIL, UPDATED_DISPLAY_NAME);

        UserProfile loadedProfile = userProfileRepository.findByIdentitySub(IDENTITY_SUB).orElseThrow();

        assertEquals(0, inserted);
        assertEquals(savedProfile.getId(), loadedProfile.getId());
        assertEquals(DISPLAY_NAME, loadedProfile.getDisplayName());
        assertEquals(EMAIL, loadedProfile.getEmail());
        assertEquals(1, userProfileRepository.count());
    }

    @Test
    void shouldInsertWhenEmailBelongsToAnotherIdentity() {
        userProfileRepository.saveAndFlush(userProfile());

        int inserted = userProfileRepository.insertIfAbsent("other-sub", EMAIL, UPDATED_DISPLAY_NAME);

        UserProfile loadedProfile = userProfileRepository.findByIdentitySub("other-sub").orElseThrow();

        assertEquals(1, inserted);
        assertEquals(EMAIL, loadedProfile.getEmail());
        assertEquals(UPDATED_DISPLAY_NAME, loadedProfile.getDisplayName());
        assertTrue(userProfileRepository.existsByIdentitySub(IDENTITY_SUB));
        assertEquals(2, userProfileRepository.count());
    }

    @Test
    void shouldThrowOnDuplicateIdentitySubConflict() {
        userProfileRepository.saveAndFlush(userProfile());

        UserProfile duplicate = UserProfileTestData.userProfile(IDENTITY_SUB, "other@gmail.com");

        assertThrows(DataIntegrityViolationException.class, () -> userProfileRepository.saveAndFlush(duplicate));
    }

    @Test
    void shouldAllowSameEmailForDifferentIdentities() {
        userProfileRepository.saveAndFlush(userProfile());

        userProfileRepository.saveAndFlush(UserProfileTestData.userProfile("other-sub", EMAIL));

        assertEquals(2, userProfileRepository.count());
    }
}
