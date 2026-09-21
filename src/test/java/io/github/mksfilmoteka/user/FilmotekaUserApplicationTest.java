package io.github.mksfilmoteka.user;

import io.github.mksfilmoteka.user.auth.KeycloakRealmRoleConverter;
import io.github.mksfilmoteka.user.auth.SecurityConfig;
import io.github.mksfilmoteka.user.catalog.CatalogClient;
import io.github.mksfilmoteka.user.common.exception.ErrorCode;
import io.github.mksfilmoteka.user.config.RepositoryTestConfig;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.ListedFilmsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;

import java.util.Set;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.*;
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static io.github.mksfilmoteka.user.util.TestUtil.JSON_MAPPER;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FilmotekaUserApplication.class)
@AutoConfigureMockMvc
@Import({RepositoryTestConfig.class, SecurityConfig.class, KeycloakRealmRoleConverter.class})
@Testcontainers(disabledWithoutDocker = true)
class FilmotekaUserApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogClient catalogClient;

    @Test
    void shouldProvisionProfileAndManageFilmListThroughApi() throws Exception {
        String profileResponse = mockMvc.perform(get(PROFILE_URL).with(authenticatedUserJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.displayName").value(DISPLAY_NAME))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode profile = JSON_MAPPER.readTree(profileResponse);
        long profileId = profile.get("id").asLong();

        mockMvc.perform(put(PROFILE_URL)
                        .with(authenticatedUserJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(updateUserProfileRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.displayName").value(UPDATED_DISPLAY_NAME));

        String createResponse = mockMvc.perform(post(FILM_LISTS_URL)
                        .with(authenticatedUserJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(filmListRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(LIST_NAME))
                .andExpect(jsonPath("$.filmIds").isArray())
                .andExpect(jsonPath("$.filmIds.length()").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode filmList = JSON_MAPPER.readTree(createResponse);
        long filmListId = filmList.get("id").asLong();

        mockMvc.perform(get(FILM_LISTS_URL + "/{id}", filmListId)
                        .with(authenticatedUserJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmListId))
                .andExpect(jsonPath("$.name").value(LIST_NAME))
                .andExpect(jsonPath("$.filmIds.length()").value(0));

        mockMvc.perform(get(FILM_LISTS_URL).with(authenticatedUserJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(filmListId))
                .andExpect(jsonPath("$[0].name").value(LIST_NAME));

        mockMvc.perform(put(FILM_LISTS_URL + "/{id}", filmListId)
                        .with(authenticatedUserJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(updateFilmListRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmListId))
                .andExpect(jsonPath("$.name").value(UPDATED_LIST_NAME));

        mockMvc.perform(post(FILM_LISTS_URL)
                        .with(authenticatedUserJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(new FilmListRequest(UPDATED_LIST_NAME))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));

        mockMvc.perform(put(FILM_LISTS_URL + "/{id}/films/{filmId}", filmListId, FILM_ID)
                        .with(authenticatedUserJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmListId))
                .andExpect(jsonPath("$.filmIds.length()").value(1))
                .andExpect(jsonPath("$.filmIds[0]").value(FILM_ID));

        ListedFilmsRequest patchFilmsRequest = new ListedFilmsRequest(Set.of(OTHER_FILM_ID), Set.of(FILM_ID));

        mockMvc.perform(patch(FILM_LISTS_URL + "/{id}/films", filmListId)
                        .with(authenticatedUserJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_MAPPER.writeValueAsString(patchFilmsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmListId))
                .andExpect(jsonPath("$.filmIds.length()").value(1))
                .andExpect(jsonPath("$.filmIds[0]").value(OTHER_FILM_ID));

        mockMvc.perform(delete(FILM_LISTS_URL + "/{id}/films/{filmId}", filmListId, OTHER_FILM_ID)
                        .with(authenticatedUserJwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(FILM_LISTS_URL + "/{id}", filmListId)
                        .with(authenticatedUserJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmListId))
                .andExpect(jsonPath("$.filmIds.length()").value(0));

        mockMvc.perform(delete(FILM_LISTS_URL + "/{id}", filmListId)
                        .with(authenticatedUserJwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(FILM_LISTS_URL + "/{id}", filmListId)
                        .with(authenticatedUserJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.name()));

        verify(catalogClient).requireFilmExists(FILM_ID);
        verify(catalogClient).requireFilmsExist(filmIds(OTHER_FILM_ID));
    }

    private static RequestPostProcessor authenticatedUserJwt() {
        return jwt().jwt(jwt -> jwt
                .subject(IDENTITY_SUB)
                .claim("email", EMAIL)
                .claim("name", DISPLAY_NAME)
                .claim("preferred_username", EMAIL));
    }
}
