package io.github.mksfilmoteka.user.profile.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String displayName
) {
}
