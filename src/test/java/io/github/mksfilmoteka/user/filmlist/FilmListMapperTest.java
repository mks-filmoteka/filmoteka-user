package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.github.mksfilmoteka.user.profile.UserProfile;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.*;
import static org.assertj.core.api.Assertions.assertThat;

class FilmListMapperTest {

    private final FilmListMapper filmListMapper = Mappers.getMapper(FilmListMapper.class);

    @Test
    void shouldMapFilmListRequestToFilmList() {
        FilmList filmList = filmListMapper.filmListRequestToFilmList(filmListRequest());

        assertThat(filmList.getName()).isEqualTo(LIST_NAME);
        assertThat(filmList.getUser()).isNull();
        assertThat(filmList.getId()).isNull();
        assertThat(filmList.getFilmIds()).isEmpty();
    }

    @Test
    void shouldMapFilmListToFilmListResponse() {
        FilmListResponse response = filmListMapper.filmListToFilmListResponse(loadedFilmList());

        assertThat(response).isEqualTo(filmListResponse());
    }

    @Test
    void shouldMapFilmListsToFilmListResponses() {
        List<FilmListResponse> responses = filmListMapper.filmListsToFilmListResponses(List.of(loadedFilmList()));

        assertThat(responses).containsExactly(filmListResponse());
    }

    @Test
    void shouldMapUpdateFilmListRequestToFilmList() {
        FilmList filmList = loadedFilmList();
        Long originalId = filmList.getId();
        UserProfile originalUser = filmList.getUser();

        filmListMapper.updateFilmListRequestToFilmList(updateFilmListRequest(), filmList);

        assertThat(filmList.getName()).isEqualTo(UPDATED_LIST_NAME);
        assertThat(filmList.getId()).isEqualTo(originalId);
        assertThat(filmList.getUser()).isSameAs(originalUser);
        assertThat(filmList.getFilmIds()).isEqualTo(filmIds());
    }
}
