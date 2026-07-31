package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.*;
import static org.assertj.core.api.Assertions.assertThat;

class FilmListItemMapperTest {

    private final FilmListItemMapper filmListItemMapper = Mappers.getMapper(FilmListItemMapper.class);

    @Test
    void shouldMapFilmListItemRequestToFilmListItem() {
        FilmListItem filmListItem = filmListItemMapper.filmListItemRequestToFilmListItem(filmListItemRequest());

        assertThat(filmListItem.getFilmId()).isEqualTo(FILM_ID);
        assertThat(filmListItem.getFilmList()).isNull();
    }

    @Test
    void shouldMapFilmListItemToFilmListItemResponse() {
        FilmListItemResponse response = filmListItemMapper.filmListItemToFilmListItemResponse(loadedFilmListItem());

        assertThat(response).isEqualTo(filmListItemResponse());
    }

    @Test
    void shouldMapFilmListItemsToFilmListItemResponses() {
        List<FilmListItemResponse> responses =
                filmListItemMapper.filmListItemsToFilmListItemResponses(List.of(loadedFilmListItem()));

        assertThat(responses).containsExactly(filmListItemResponse());
    }
}
