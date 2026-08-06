package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileProvisionService provisioningService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void shouldGetUserProfile() {
        AuthUser authUser = authUser();
        UserProfile userProfile = loadedUserProfile();
        UserProfileResponse expectedResponse = userProfileResponse();

        when(provisioningService.getOrCreate(authUser)).thenReturn(userProfile);
        when(userProfileMapper.userProfileToUserProfileResponse(userProfile)).thenReturn(expectedResponse);

        UserProfileResponse response = userProfileService.getUserProfile(authUser);

        assertThat(response).isEqualTo(expectedResponse);

        verify(provisioningService).getOrCreate(authUser);
        verify(userProfileMapper).userProfileToUserProfileResponse(userProfile);
        verifyNoInteractions(userProfileRepository);
    }

    @Test
    void shouldUpdateUserProfile() {
        AuthUser authUser = authUser();
        UserProfile userProfile = loadedUserProfile();
        UserProfileRequest request = updateUserProfileRequest();
        UserProfileResponse expectedResponse = updateUserProfileResponse();

        when(provisioningService.getOrCreate(authUser)).thenReturn(userProfile);
        doAnswer(updateDisplayNameOnly())
                .when(userProfileMapper)
                .updateUserProfileRequestToUserProfile(request, userProfile);
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);
        when(userProfileMapper.userProfileToUserProfileResponse(userProfile)).thenReturn(expectedResponse);

        UserProfileResponse response = userProfileService.updateUserProfile(authUser, request);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(userProfile.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);

        verify(provisioningService).getOrCreate(authUser);
        verify(userProfileMapper).updateUserProfileRequestToUserProfile(request, userProfile);
        verify(userProfileRepository).save(userProfile);
        verify(userProfileMapper).userProfileToUserProfileResponse(userProfile);
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
