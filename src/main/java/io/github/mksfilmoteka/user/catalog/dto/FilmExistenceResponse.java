package io.github.mksfilmoteka.user.catalog.dto;

import java.util.Set;

public record FilmExistenceResponse(
        Set<Long> missingFilmIds
) {
}
