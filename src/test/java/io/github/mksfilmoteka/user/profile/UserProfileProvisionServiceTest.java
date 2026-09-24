package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileProvisionServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileProvisionService provisionService;

    @Test
    void shouldReturnExistingUserProfile() {
        UserProfile userProfile = loadedUserProfile();
        AuthUser authUser = new AuthUser(IDENTITY_SUB, EMAIL, DISPLAY_NAME);

        when(userProfileRepository.findByIdentitySub(IDENTITY_SUB)).thenReturn(Optional.of(userProfile));

        UserProfile result = provisionService.getOrCreate(authUser);

        assertThat(result).isSameAs(userProfile);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getDisplayName()).isEqualTo(DISPLAY_NAME);

        verify(userProfileRepository).findByIdentitySub(IDENTITY_SUB);
        verify(userProfileRepository, never()).insertIfAbsent(anyString(), anyString(), anyString());
        verify(userProfileRepository, never()).existsByEmail(anyString());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void shouldCreateUserProfile() {
        UserProfile userProfile = loadedUserProfile();
        AuthUser authUser = new AuthUser(IDENTITY_SUB, EMAIL, DISPLAY_NAME);

        when(userProfileRepository.findByIdentitySub(IDENTITY_SUB))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(userProfile));

        when(userProfileRepository.insertIfAbsent(IDENTITY_SUB, EMAIL, DISPLAY_NAME)).thenReturn(1);

        UserProfile result = provisionService.getOrCreate(authUser);

        assertThat(result).isSameAs(userProfile);
        assertThat(result.getId()).isEqualTo(USER_PROFILE_ID);
        assertThat(result.getIdentitySub()).isEqualTo(IDENTITY_SUB);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getDisplayName()).isEqualTo(DISPLAY_NAME);

        verify(userProfileRepository).insertIfAbsent(IDENTITY_SUB, EMAIL, DISPLAY_NAME);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void shouldReturnProfileCreatedByAnotherRequest() {
        UserProfile existingProfile = loadedUserProfile();
        AuthUser authUser = new AuthUser(IDENTITY_SUB, EMAIL, DISPLAY_NAME);

        when(userProfileRepository.findByIdentitySub(IDENTITY_SUB))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingProfile));

        when(userProfileRepository.insertIfAbsent(IDENTITY_SUB, EMAIL, DISPLAY_NAME)).thenReturn(0);

        UserProfile result = provisionService.getOrCreate(authUser);

        assertThat(result).isSameAs(existingProfile);
        assertThat(result.getIdentitySub()).isEqualTo(IDENTITY_SUB);

        verify(userProfileRepository).insertIfAbsent(IDENTITY_SUB, EMAIL, DISPLAY_NAME);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void shouldSynchronizeChangedEmail() {
        UserProfile userProfile = loadedUserProfile();
        AuthUser authUser = new AuthUser(IDENTITY_SUB, NEW_EMAIL, "token display name");

        when(userProfileRepository.findByIdentitySub(IDENTITY_SUB)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);

        UserProfile result = provisionService.getOrCreate(authUser);

        assertThat(result).isSameAs(userProfile);
        assertThat(result.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(result.getDisplayName()).isEqualTo(DISPLAY_NAME);

        verify(userProfileRepository).findByIdentitySub(IDENTITY_SUB);
        verify(userProfileRepository).existsByEmail(NEW_EMAIL);
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void shouldRejectCreationWhenEmailAlreadyExists() {
        AuthUser authUser = new AuthUser(IDENTITY_SUB, EMAIL, DISPLAY_NAME);

        when(userProfileRepository.findByIdentitySub(IDENTITY_SUB)).thenReturn(Optional.empty());
        when(userProfileRepository.insertIfAbsent(IDENTITY_SUB, EMAIL, DISPLAY_NAME)).thenReturn(0);

        assertThrows(ConflictException.class, () -> provisionService.getOrCreate(authUser));

        verify(userProfileRepository).insertIfAbsent(IDENTITY_SUB, EMAIL, DISPLAY_NAME);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void shouldRejectEmailSynchronizationWhenEmailAlreadyExists() {
        UserProfile userProfile = loadedUserProfile();
        AuthUser authUser = new AuthUser(IDENTITY_SUB, NEW_EMAIL, "token display name");

        when(userProfileRepository.findByIdentitySub(IDENTITY_SUB)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThrows(ConflictException.class, () -> provisionService.getOrCreate(authUser));
        assertThat(userProfile.getEmail()).isEqualTo(EMAIL);

        verify(userProfileRepository).existsByEmail(NEW_EMAIL);
        verify(userProfileRepository, never()).save(any());
    }
}
