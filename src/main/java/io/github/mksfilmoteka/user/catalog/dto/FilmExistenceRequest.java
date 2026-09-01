package io.github.mksfilmoteka.user.catalog.dto;

import java.util.Set;

public record FilmExistenceRequest(
        Set<Long> filmIds
) {
}
