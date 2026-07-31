package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void shouldFindUserProfileByIdIfExists() {
        UserProfile userProfile = loadedUserProfile();

        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.of(userProfile));
        when(userProfileMapper.userProfileToUserProfileResponse(userProfile)).thenReturn(userProfileResponse());

        UserProfileResponse response = userProfileService.findById(USER_PROFILE_ID);

        assertThat(response).isEqualTo(userProfileResponse());

        verify(userProfileRepository).findById(USER_PROFILE_ID);
        verify(userProfileMapper).userProfileToUserProfileResponse(userProfile);
    }

    @Test
    void shouldThrowOnFindByIdIfUserProfileDoesNotExist() {
        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userProfileService.findById(USER_PROFILE_ID));

        verify(userProfileRepository).findById(USER_PROFILE_ID);
        verifyNoInteractions(userProfileMapper);
    }

    @Test
    void shouldUpdateUserProfileIfExists() {
        UserProfile userProfile = loadedUserProfile();
        UserProfileRequest request = updateUserProfileRequest();

        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.of(userProfile));
        doAnswer(updateDisplayNameOnly()).when(userProfileMapper)
                .updateUserProfileRequestToUserProfile(request, userProfile);
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);
        when(userProfileMapper.userProfileToUserProfileResponse(userProfile)).thenReturn(updateUserProfileResponse());

        UserProfileResponse response = userProfileService.updateUserProfile(USER_PROFILE_ID, request);

        assertThat(response).isEqualTo(updateUserProfileResponse());
        assertThat(userProfile.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);

        verify(userProfileMapper).updateUserProfileRequestToUserProfile(request, userProfile);
        verify(userProfileRepository).save(userProfile);
        verify(userProfileMapper).userProfileToUserProfileResponse(userProfile);
    }

    @Test
    void shouldThrowOnUpdateIfUserProfileDoesNotExist() {
        UserProfileRequest request = userProfileRequest();

        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userProfileService.updateUserProfile(USER_PROFILE_ID, request));

        verify(userProfileRepository).findById(USER_PROFILE_ID);
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(userProfileMapper);
    }

    private static Answer<Void> updateDisplayNameOnly() {
        return invocation -> {
            UserProfileRequest request = invocation.getArgument(0);
            UserProfile userProfile = invocation.getArgument(1);

            userProfile.setDisplayName(request.displayName());

            return null;
        };
    }
}
