package io.github.mksfilmoteka.user.catalog;

import io.github.mksfilmoteka.user.catalog.dto.FilmExistenceRequest;
import io.github.mksfilmoteka.user.catalog.dto.FilmExistenceResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/films")
public interface CatalogApi {

    @PostExchange("/existence")
    FilmExistenceResponse checkFilmsExistence(@RequestBody FilmExistenceRequest request);
}
