package io.github.mksfilmoteka.user.filmlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Film list response")
public record FilmListResponse(
        @Schema(description = "Film list id", example = "1")
        Long id,

        @Schema(description = "Film list name", example = "Favorites")
        String name,

        @Schema(description = "Film ids in the list", example = "[100, 200]")
        Set<Long> filmIds
) {
}
