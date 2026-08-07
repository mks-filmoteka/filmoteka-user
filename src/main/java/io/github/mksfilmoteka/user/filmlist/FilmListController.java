package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.auth.AuthUserConverter;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/film-lists")
@RequiredArgsConstructor
public class FilmListController {

    private final FilmListService filmListService;
    private final AuthUserConverter authUserConverter;

    @GetMapping
    public ResponseEntity<List<FilmListResponse>> getFilmLists(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(filmListService.getFilmLists(authUser(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmListResponse> getFilmList(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        return ResponseEntity.ok(filmListService.findById(authUser(jwt), id));
    }

    @PostMapping
    public ResponseEntity<FilmListResponse> createFilmList(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FilmListRequest request) {

        FilmListResponse response = filmListService.createFilmList(authUser(jwt), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FilmListResponse> updateFilmList(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody FilmListRequest request) {

        return ResponseEntity.ok(filmListService.updateFilmList(authUser(jwt), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilmList(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        filmListService.deleteFilmList(authUser(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/films/{filmId}")
    public ResponseEntity<FilmListResponse> addFilm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @PathVariable Long filmId) {

        return ResponseEntity.ok(filmListService.addFilm(authUser(jwt), id, filmId));
    }

    @DeleteMapping("/{id}/films/{filmId}")
    public ResponseEntity<Void> removeFilm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @PathVariable Long filmId) {

        filmListService.removeFilm(authUser(jwt), id, filmId);
        return ResponseEntity.noContent().build();
    }

    private AuthUser authUser(Jwt jwt) {
        return authUserConverter.from(jwt);
    }
}
