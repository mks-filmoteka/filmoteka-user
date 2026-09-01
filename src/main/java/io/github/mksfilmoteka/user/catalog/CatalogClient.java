package io.github.mksfilmoteka.user.catalog;

import io.github.mksfilmoteka.user.catalog.dto.FilmExistenceRequest;
import io.github.mksfilmoteka.user.catalog.dto.FilmExistenceResponse;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.common.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final CatalogApi catalogApi;

    public void requireFilmExists(Long filmId) {
        requireFilmsExist(Set.of(filmId));
    }

    public void requireFilmsExist(Set<Long> filmIds) {
        if (filmIds.isEmpty()) return;
        FilmExistenceResponse response;

        try {
            response = catalogApi.checkFilmsExistence(new FilmExistenceRequest(filmIds));
        } catch (RestClientException ex) {
            log.warn("Catalog request failed while validating film ids={}", filmIds, ex);
            throw new ServiceUnavailableException("Catalog service is unavailable", ex);
        }

        if (!response.missingFilmIds().isEmpty()) {
            throw new ResourceNotFoundException("Films not found: " + response.missingFilmIds());
        }
    }
}
