package io.github.mksfilmoteka.user.filmlist.dto;

import java.time.Instant;
import java.util.UUID;

public record FilmDeletedEvent(
        UUID eventId,
        Long filmId,
        String posterName,
        Instant occurredAt
) {
}
