package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.auth.AuthUserConverter;
import io.github.mksfilmoteka.user.auth.KeycloakRealmRoleConverter;
import io.github.mksfilmoteka.user.auth.SecurityConfig;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.*;
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.AUTH_USER;
import static io.github.mksfilmoteka.user.util.TestUtil.JSON_MAPPER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmListController.class)
@Import({SecurityConfig.class, KeycloakRealmRoleConverter.class})
class FilmListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FilmListService filmListService;

    @MockitoBean
    private AuthUserConverter authUserConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectUnauthenticatedFilmListRequest() throws Exception {
        mockMvc.perform(get(FILM_LISTS_URL)).andExpect(status().isUnauthorized());

        verifyNoInteractions(authUserConverter, filmListService);
    }

    @Test
    void shouldGetFilmLists() throws Exception {
        List<FilmListResponse> expectedResponses = List.of(filmListResponse());

        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);
        when(filmListService.getFilmLists(AUTH_USER)).thenReturn(expectedResponses);

        mockMvc.perform(get(FILM_LISTS_URL).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(JSON_MAPPER.writeValueAsString(expectedResponses)));

        verify(authUserConverter).from(any(Jwt.class));
        verify(filmListService).getFilmLists(AUTH_USER);
    }

    @Test
    void shouldGetFilmListById() throws Exception {
        FilmListResponse expectedResponse = filmListResponse();

        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);
        when(filmListService.findById(AUTH_USER, LIST_ID)).thenReturn(expectedResponse);

        mockMvc.perform(get(FILM_LISTS_URL + "/{id}", LIST_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(JSON_MAPPER.writeValueAsString(expectedResponse)));

        verify(authUserConverter).from(any(Jwt.class));
        verify(filmListService).findById(AUTH_USER, LIST_ID);
    }

    @Test
    void shouldCreateFilmList() throws Exception {
        FilmListRequest request = filmListRequest();
        FilmListResponse expectedResponse = filmListResponse();

        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);
        when(filmListService.createFilmList(AUTH_USER, request)).thenReturn(expectedResponse);

        mockMvc.perform(post(FILM_LISTS_URL).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(JSON_MAPPER.writeValueAsString(expectedResponse)));

        verify(authUserConverter).from(any(Jwt.class));
        verify(filmListService).createFilmList(AUTH_USER, request);
    }

    @Test
    void shouldRejectInvalidFilmListCreate() throws Exception {
        mockMvc.perform(post(FILM_LISTS_URL).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(new FilmListRequest(""))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authUserConverter, filmListService);
    }

    @Test
    void shouldUpdateFilmList() throws Exception {
        FilmListRequest request = updateFilmListRequest();
        FilmListResponse expectedResponse = filmListResponse();

        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);
        when(filmListService.updateFilmList(AUTH_USER, LIST_ID, request)).thenReturn(expectedResponse);

        mockMvc.perform(put(FILM_LISTS_URL + "/{id}", LIST_ID).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(JSON_MAPPER.writeValueAsString(expectedResponse)));

        verify(authUserConverter).from(any(Jwt.class));
        verify(filmListService).updateFilmList(AUTH_USER, LIST_ID, request);
    }

    @Test
    void shouldRejectInvalidFilmListUpdate() throws Exception {
        mockMvc.perform(put(FILM_LISTS_URL + "/{id}", LIST_ID).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(new FilmListRequest(""))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authUserConverter, filmListService);
    }

    @Test
    void shouldDeleteFilmList() throws Exception {
        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);

        mockMvc.perform(delete(FILM_LISTS_URL + "/{id}", LIST_ID).with(jwt()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(authUserConverter).from(any(Jwt.class));
        verify(filmListService).deleteFilmList(AUTH_USER, LIST_ID);
    }

    @Test
    void shouldAddFilmToFilmList() throws Exception {
        FilmListResponse expectedResponse = filmListResponse();

        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);
        when(filmListService.addFilm(AUTH_USER, LIST_ID, FILM_ID)).thenReturn(expectedResponse);

        mockMvc.perform(put(FILM_LISTS_URL + "/{id}/films/{filmId}", LIST_ID, FILM_ID).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(JSON_MAPPER.writeValueAsString(expectedResponse)));

        verify(authUserConverter).from(any(Jwt.class));
        verify(filmListService).addFilm(AUTH_USER, LIST_ID, FILM_ID);
    }

    @Test
    void shouldRemoveFilmFromFilmList() throws Exception {
        when(authUserConverter.from(any(Jwt.class))).thenReturn(AUTH_USER);

        mockMvc.perform(delete(FILM_LISTS_URL + "/{id}/films/{filmId}", LIST_ID, FILM_ID).with(jwt()))
                .andExpect(status().isNoContent());

        verify(authUserConverter).from(any(Jwt.class));
        verify(filmListService).removeFilm(AUTH_USER, LIST_ID, FILM_ID);
    }
}
