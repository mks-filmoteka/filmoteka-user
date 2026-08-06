package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.auth.AuthUserConverter;
import io.github.mksfilmoteka.user.auth.KeycloakRealmRoleConverter;
import io.github.mksfilmoteka.user.auth.SecurityConfig;
import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@Import({SecurityConfig.class, KeycloakRealmRoleConverter.class})
class UserProfileControllerTest {

    private static final String PROFILE_URL = "/api/v1/profile";

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private AuthUserConverter authUserConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectUnauthenticatedProfileRequest() throws Exception {
        mockMvc.perform(get(PROFILE_URL)).andExpect(status().isUnauthorized());

        verifyNoInteractions(authUserConverter, userProfileService);
    }

    @Test
    void shouldGetAuthenticatedUserProfile() throws Exception {
        AuthUser authUser = authUser();
        UserProfileResponse expectedResponse = userProfileResponse();

        when(authUserConverter.from(any(Jwt.class))).thenReturn(authUser);
        when(userProfileService.getUserProfile(authUser)).thenReturn(expectedResponse);

        mockMvc.perform(get(PROFILE_URL)
                        .with(jwt().jwt(jwt -> jwt
                                .subject(IDENTITY_SUB)
                                .claim("email", EMAIL))
                        ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonMapper.writeValueAsString(expectedResponse)));

        verify(authUserConverter).from(any(Jwt.class));
        verify(userProfileService).getUserProfile(authUser);
    }

    @Test
    void shouldUpdateAuthenticatedUserProfile() throws Exception {
        AuthUser authUser = authUser();
        UserProfileRequest request = updateUserProfileRequest();
        UserProfileResponse expectedResponse = updateUserProfileResponse();

        when(authUserConverter.from(any(Jwt.class))).thenReturn(authUser);
        when(userProfileService.updateUserProfile(authUser, request)).thenReturn(expectedResponse);

        mockMvc.perform(put(PROFILE_URL)
                        .with(jwt().jwt(jwt -> jwt
                                .subject(IDENTITY_SUB)
                                .claim("email", EMAIL)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonMapper.writeValueAsString(expectedResponse)));

        verify(authUserConverter).from(any(Jwt.class));
        verify(userProfileService).updateUserProfile(authUser, request);
    }

    @Test
    void shouldRejectInvalidProfileUpdate() throws Exception {
        mockMvc.perform(put(PROFILE_URL)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new UserProfileRequest(""))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authUserConverter, userProfileService);
    }
}