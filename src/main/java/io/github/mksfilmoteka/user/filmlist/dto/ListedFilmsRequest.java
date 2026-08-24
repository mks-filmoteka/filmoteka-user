package io.github.mksfilmoteka.user.filmlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Collections;
import java.util.Set;

@Schema(description = "Request for adding and removing film ids in a film list")
public record ListedFilmsRequest(
        @NotNull
        @Schema(description = "Film ids to add", example = "[100, 200]")
        Set<@NotNull @Positive Long> addedFilmIds,

        @NotNull
        @Schema(description = "Film ids to remove", example = "[300]")
        Set<@NotNull @Positive Long> removedFilmIds
) {
    @AssertTrue(message = "The same film id cannot be both added and removed")
    @Schema(hidden = true)
    public boolean isDisjoint() {
        return addedFilmIds == null  || removedFilmIds == null || Collections.disjoint(addedFilmIds, removedFilmIds);
    }
}
