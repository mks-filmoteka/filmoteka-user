package io.github.mksfilmoteka.user.profile;

public final class UserProfileTestData {
    public static final String IDENTITY_SUB = "test-sub";
    public static final String EMAIL = "test@gmail.com";
    public static final String DISPLAY_NAME = "Test User";
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
}
