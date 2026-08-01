package io.github.mksfilmoteka.user.filmlist.dto;

import java.util.Set;

public record FilmListResponse(
        Long id,
        String name,
        Set<Long> filmIds
) {
}
