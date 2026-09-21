package io.github.mksfilmoteka.user.filmlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import tools.jackson.core.JacksonException;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.FILM_ID;
import static io.github.mksfilmoteka.user.util.TestUtil.JSON_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmDeletedListenerTest {

    @Mock
    private FilmListService filmListService;

    private FilmDeletedListener filmDeletedListener;

    @BeforeEach
    void setUp() {
        filmDeletedListener = new FilmDeletedListener(JSON_MAPPER, filmListService);
    }

    @Test
    void shouldRemoveDeletedFilmFromAllLists() {
        String payload = """
                {
                  "eventId": "c58931cd-5951-4cac-98e5-84f0433ef4c5",
                  "filmId": %d,
                  "posterName": "poster.jpg",
                  "occurredAt": "2026-09-17T12:00:00Z"
                }
                """.formatted(FILM_ID);

        filmDeletedListener.onFilmDeleted(payload);

        verify(filmListService).removeDeletedFilmFromAllLists(FILM_ID);
        verifyNoMoreInteractions(filmListService);
    }

    @Test
    void shouldProcessEventWithOnlyFilmId() {
        filmDeletedListener.onFilmDeleted("{\"filmId\": %d}".formatted(FILM_ID));

        verify(filmListService).removeDeletedFilmFromAllLists(FILM_ID);
    }

    @Test
    void shouldRejectMalformedJson() {
        assertThrows(JacksonException.class, () -> filmDeletedListener.onFilmDeleted("{invalid-json"));

        verifyNoInteractions(filmListService);
    }

    @Test
    void shouldRejectNullEvent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> filmDeletedListener.onFilmDeleted("null"));

        assertThat(exception).hasMessage("FilmDeletedEvent must contain a positive filmId");
        verifyNoInteractions(filmListService);
    }

    @Test
    void shouldRejectNullFilmId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> filmDeletedListener.onFilmDeleted("{\"filmId\": null}"));

        assertThat(exception).hasMessage("FilmDeletedEvent must contain a positive filmId");
        verifyNoInteractions(filmListService);
    }

    @Test
    void shouldPropagateServiceFailureForKafkaRetry() {
        String payload = "{\"filmId\": %d}".formatted(FILM_ID);
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("Database unavailable");
        doThrow(failure).when(filmListService).removeDeletedFilmFromAllLists(FILM_ID);

        DataAccessResourceFailureException exception = assertThrows(DataAccessResourceFailureException.class,
                () -> filmDeletedListener.onFilmDeleted(payload));

        assertThat(exception).isSameAs(failure);
    }
}
