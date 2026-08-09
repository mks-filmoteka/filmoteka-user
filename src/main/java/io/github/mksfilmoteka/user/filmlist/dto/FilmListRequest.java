package io.github.mksfilmoteka.user.filmlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a film list")
public record FilmListRequest(
        @Schema(description = "Film list name", example = "Favorites")
        @NotBlank
        @Size(max = 100)
        String name
) {
}
