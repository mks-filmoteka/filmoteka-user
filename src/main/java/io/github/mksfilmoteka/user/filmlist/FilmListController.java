package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.auth.AuthUserConverter;
import io.github.mksfilmoteka.user.common.exception.ErrorResponse;
import io.github.mksfilmoteka.user.filmlist.dto.ListedFilmsRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Film Lists", description = "Operations related to authenticated user's film lists")
@RestController
@RequestMapping("/api/v1/film-lists")
@RequiredArgsConstructor
public class FilmListController {

    private final FilmListService filmListService;
    private final AuthUserConverter authUserConverter;

    @Operation(
            summary = "Get film lists",
            description = "Returns all film lists owned by the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Film lists returned",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FilmListResponse.class))
            )
    )
    @GetMapping
    public ResponseEntity<List<FilmListResponse>> getFilmLists(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(filmListService.getFilmLists(authUser(jwt)));
    }

    @Operation(
            summary = "Get film list by id",
            description = "Returns a film list owned by the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Film list returned",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FilmListResponse.class)
            )
    )
    @ApiResponse(responseCode = "404", description = "Film list not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/{id}")
    public ResponseEntity<FilmListResponse> getFilmList(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        return ResponseEntity.ok(filmListService.findById(authUser(jwt), id));
    }

    @Operation(
            summary = "Create film list",
            description = "Creates a film list for the authenticated user"
    )
    @ApiResponse(responseCode = "201", description = "Film list created",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FilmListResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(responseCode = "409", description = "Film list name conflict",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping
    public ResponseEntity<FilmListResponse> createFilmList(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FilmListRequest request) {

        FilmListResponse response = filmListService.createFilmList(authUser(jwt), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Update film list",
            description = "Updates film list fields"
    )
    @ApiResponse(responseCode = "200", description = "Film list updated",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FilmListResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(responseCode = "404", description = "Film list not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(responseCode = "409", description = "Film list name conflict",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<FilmListResponse> updateFilmList(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody FilmListRequest request) {

        return ResponseEntity.ok(filmListService.updateFilmList(authUser(jwt), id, request));
    }

    @Operation(
            summary = "Delete film list",
            description = "Deletes a film list by id"
    )
    @ApiResponse(responseCode = "204", description = "Film list deleted")
    @ApiResponse(responseCode = "404", description = "Film list not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilmList(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        filmListService.deleteFilmList(authUser(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Add film to film list",
            description = "Adds a film id to a film list owned by the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Film added to film list",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FilmListResponse.class)
            )
    )
    @ApiResponse(responseCode = "404", description = "Film list not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PutMapping("/{id}/films/{filmId}")
    public ResponseEntity<FilmListResponse> addFilm(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @PathVariable Long filmId) {

        return ResponseEntity.ok(filmListService.addFilm(authUser(jwt), id, filmId));
    }

    @Operation(
            summary = "Patch films in film list",
            description = "Adds and removes film ids in a film list owned by the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Film list films patched",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FilmListResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(responseCode = "404", description = "Film list or film entries not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PatchMapping("/{id}/films")
    public ResponseEntity<FilmListResponse> patchFilms(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody ListedFilmsRequest request) {

        return ResponseEntity.ok(filmListService.patchFilms(authUser(jwt), id, request));
    }

    @Operation(
            summary = "Remove film from film list",
            description = "Removes a film id from a film list owned by the authenticated user"
    )
    @ApiResponse(responseCode = "204", description = "Film removed from film list")
    @ApiResponse(responseCode = "404", description = "Film list or film entry not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @DeleteMapping("/{id}/films/{filmId}")
    public ResponseEntity<Void> removeFilm(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @PathVariable Long filmId) {

        filmListService.removeFilm(authUser(jwt), id, filmId);
        return ResponseEntity.noContent().build();
    }

    private AuthUser authUser(Jwt jwt) {
        return authUserConverter.from(jwt);
    }
}
