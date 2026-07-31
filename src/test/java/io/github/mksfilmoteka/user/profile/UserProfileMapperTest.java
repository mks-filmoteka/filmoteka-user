package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserProfileMapperTest {

    private final UserProfileMapper userProfileMapper = Mappers.getMapper(UserProfileMapper.class);

    @Test
    void shouldMapUserProfileRequestToUserProfile() {
        UserProfile userProfile = userProfileMapper.userProfileRequestToUserProfile(userProfileRequest());

        assertThat(userProfile.getDisplayName()).isEqualTo(DISPLAY_NAME);
    }

    @Test
    void shouldMapUserProfileToUserProfileResponse() {
        UserProfileResponse response = userProfileMapper.userProfileToUserProfileResponse(loadedUserProfile());

        assertThat(response).isEqualTo(userProfileResponse());
    }

    @Test
    void shouldMapUpdateUserProfileRequestToUserProfile() {
        UserProfile userProfile = loadedUserProfile();

        userProfileMapper.updateUserProfileRequestToUserProfile(updateUserProfileRequest(), userProfile);

        assertThat(userProfile.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
    }
}
