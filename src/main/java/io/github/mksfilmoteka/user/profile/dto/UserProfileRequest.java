package io.github.mksfilmoteka.user.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileRequest(

        @NotBlank
        @Size(max = 100)
        String displayName
) {
}
