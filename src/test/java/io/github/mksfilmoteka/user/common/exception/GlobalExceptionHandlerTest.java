package io.github.mksfilmoteka.user.common.exception;

import io.github.mksfilmoteka.user.auth.AuthUserConverter;
import io.github.mksfilmoteka.user.auth.KeycloakRealmRoleConverter;
import io.github.mksfilmoteka.user.auth.SecurityConfig;
import io.github.mksfilmoteka.user.filmlist.FilmListController;
import io.github.mksfilmoteka.user.filmlist.FilmListService;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.FILM_LISTS_URL;
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.AUTH_USER;
import static io.github.mksfilmoteka.user.util.TestUtil.JSON_MAPPER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmListController.class)
@Import({SecurityConfig.class, KeycloakRealmRoleConverter.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FilmListService filmListService;

    @MockitoBean
    private AuthUserConverter authUserConverter;

    @Test
    void shouldReturnNotFoundForUnknownPath() throws Exception {
        mockMvc.perform(get("/api/v1/unknown").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/unknown"))
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.name()));
    }

    @Test
    void shouldReturnMethodNotAllowedForUnsupportedMethod() throws Exception {
        mockMvc.perform(patch(FILM_LISTS_URL + "/1").with(jwt()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value(ErrorCode.METHOD_NOT_ALLOWED.name()));
    }

    @Test
    void shouldReturnUnsupportedMediaTypeForWrongContentType() throws Exception {
        mockMvc.perform(post(FILM_LISTS_URL)
                        .with(jwt())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNSUPPORTED_MEDIA_TYPE.name()));
    }

    @Test
    void shouldReturnBadRequestWithoutParserDetailsForMalformedBody() throws Exception {
        mockMvc.perform(post(FILM_LISTS_URL)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.name()));
    }

    @Test
    void shouldReturnConflictForDataIntegrityViolation() throws Exception {
        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);
        when(filmListService.createFilmList(eq(AUTH_USER), any(FilmListRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key violates idx_film_list_user_name"));

        mockMvc.perform(post(FILM_LISTS_URL)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(new FilmListRequest("Favourites"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", not(containsString("idx_film_list_user_name"))))
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));
    }

    @Test
    void shouldReturnConflictForOptimisticLockingFailure() throws Exception {
        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);
        when(filmListService.createFilmList(eq(AUTH_USER), any(FilmListRequest.class)))
                .thenThrow(new OptimisticLockingFailureException("stale"));

        mockMvc.perform(post(FILM_LISTS_URL)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(new FilmListRequest("Favourites"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));
    }
}
