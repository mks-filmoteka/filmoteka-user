package io.github.mksfilmoteka.user.filmlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FilmListRequest(
        @NotBlank
        @Size(max = 100)
        String name
) {
}
