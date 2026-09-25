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
        Set<Long> missingFilmIds = findMissingFilmIds(filmIds);

        if (!missingFilmIds.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Films not found: " + missingFilmIds
            );
        }
    }

    public Set<Long> findMissingFilmIds(Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Set.of();
        }
        FilmExistenceResponse response;
        try {
            response = catalogApi.checkFilmsExistence(new FilmExistenceRequest(filmIds));
        } catch (RestClientException ex) {
            log.warn("Catalog request failed while checking film ids={}", filmIds, ex);
            throw new ServiceUnavailableException("Catalog service is unavailable", ex);
        }
        if (response == null || response.missingFilmIds() == null) {
            throw new ServiceUnavailableException("Catalog returned an invalid film existence response");
        }
        return response.missingFilmIds();
    }
}
