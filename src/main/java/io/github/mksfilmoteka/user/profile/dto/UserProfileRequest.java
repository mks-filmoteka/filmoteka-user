package io.github.mksfilmoteka.user.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for updating a user profile")
public record UserProfileRequest(

        @Schema(description = "User display name", example = "Display Name")
        @NotBlank
        @Size(max = 100)
        String displayName
) {
}
