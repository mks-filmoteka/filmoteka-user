package io.github.mksfilmoteka.user.catalog;

import io.github.mksfilmoteka.user.catalog.dto.FilmExistenceRequest;
import io.github.mksfilmoteka.user.catalog.dto.FilmExistenceResponse;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.common.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.util.Set;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.FILM_ID;
import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.OTHER_FILM_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogClientTest {

    @Mock
    private CatalogApi catalogApi;

    @InjectMocks
    private CatalogClient catalogClient;

    @Test
    void shouldRequireFilmExists() {
        when(catalogApi.checkFilmsExistence(new FilmExistenceRequest(Set.of(FILM_ID))))
                .thenReturn(new FilmExistenceResponse(Set.of()));

        catalogClient.requireFilmExists(FILM_ID);

        verify(catalogApi).checkFilmsExistence(new FilmExistenceRequest(Set.of(FILM_ID)));
    }

    @Test
    void shouldRequireFilmsExist() {
        Set<Long> filmIds = Set.of(FILM_ID, OTHER_FILM_ID);

        when(catalogApi.checkFilmsExistence(new FilmExistenceRequest(filmIds)))
                .thenReturn(new FilmExistenceResponse(Set.of()));

        catalogClient.requireFilmsExist(filmIds);

        verify(catalogApi).checkFilmsExistence(new FilmExistenceRequest(filmIds));
    }

    @Test
    void shouldSkipCatalogRequestForEmptyFilmIds() {
        catalogClient.requireFilmsExist(Set.of());

        verifyNoInteractions(catalogApi);
    }

    @Test
    void shouldTranslateMissingFilms() {
        when(catalogApi.checkFilmsExistence(new FilmExistenceRequest(Set.of(FILM_ID))))
                .thenReturn(new FilmExistenceResponse(Set.of(FILM_ID)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> catalogClient.requireFilmExists(FILM_ID));

        assertThat(exception).hasMessage("Films not found: [" + FILM_ID + "]");
    }

    @Test
    void shouldTranslateCatalogFailure() {
        RestClientException cause = new RestClientException("Connection refused");
        doThrow(cause).when(catalogApi).checkFilmsExistence(new FilmExistenceRequest(Set.of(FILM_ID)));

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> catalogClient.requireFilmExists(FILM_ID));

        assertThat(exception)
                .hasMessage("Catalog service is unavailable")
                .hasCause(cause);
    }
}
