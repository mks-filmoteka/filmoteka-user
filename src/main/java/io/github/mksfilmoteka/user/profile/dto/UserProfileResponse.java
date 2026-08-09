package io.github.mksfilmoteka.user.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User profile response")
public record UserProfileResponse(
        @Schema(description = "User profile id", example = "1")
        Long id,

        @Schema(description = "User email", example = "test@example.com")
        String email,

        @Schema(description = "User display name", example = "Display Name")
        String displayName
) {
}
