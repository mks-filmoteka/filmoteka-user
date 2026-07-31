package io.github.mksfilmoteka.user.filmlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FilmListItemRequest(
        @NotNull
        @Positive
        Long filmId
) {
}
