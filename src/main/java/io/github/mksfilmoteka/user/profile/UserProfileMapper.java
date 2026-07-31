package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfile userProfileRequestToUserProfile(UserProfileRequest request);

    void updateUserProfileRequestToUserProfile(UserProfileRequest request, @MappingTarget UserProfile userProfile);

    UserProfileResponse userProfileToUserProfileResponse(UserProfile userProfile);
}
