package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;

public final class UserProfileTestData {
    public static final String IDENTITY_SUB = "test-sub";
    public static final String EMAIL = "test@gmail.com";
    public static final String NEW_EMAIL = "new-email@gmail.com";
    public static final String DISPLAY_NAME = "Test User";
    public static final String UPDATED_DISPLAY_NAME = "updated name";
    public static final long USER_PROFILE_ID = 1L;

    public static UserProfile userProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setIdentitySub(IDENTITY_SUB);
        userProfile.setEmail(EMAIL);
        userProfile.setDisplayName(DISPLAY_NAME);
        return userProfile;
    }

    public static UserProfile loadedUserProfile() {
        UserProfile userProfile = userProfile();
        userProfile.setId(USER_PROFILE_ID);
        return userProfile;
    }

    public static UserProfile userProfile(String identitySub, String email) {
        UserProfile userProfile = userProfile();
        userProfile.setIdentitySub(identitySub);
        userProfile.setEmail(email);
        return userProfile;
    }

    public static UserProfileRequest userProfileRequest() {
        return new UserProfileRequest(DISPLAY_NAME);
    }

    public static UserProfileRequest updateUserProfileRequest() {
        return new UserProfileRequest(UPDATED_DISPLAY_NAME);
    }

    public static UserProfileResponse userProfileResponse() {
        return new UserProfileResponse(USER_PROFILE_ID, EMAIL, DISPLAY_NAME);
    }

    public static UserProfileResponse updateUserProfileResponse() {
        return new UserProfileResponse(USER_PROFILE_ID, EMAIL, UPDATED_DISPLAY_NAME);
    }
}
