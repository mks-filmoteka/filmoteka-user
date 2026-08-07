package io.github.mksfilmoteka.user.auth;

import io.github.mksfilmoteka.user.common.exception.InvalidAuthenticationClaimsException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static io.github.mksfilmoteka.user.util.TestUtil.jwt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUserConverterTest {

    private final AuthUserConverter converter = new AuthUserConverter();

    @Test
    void shouldMapIdentityClaims() {
        Jwt jwt = jwt(Map.of(
                "sub", IDENTITY_SUB,
                "email", EMAIL,
                "name", DISPLAY_NAME,
                "preferred_username", EMAIL
        ));

        AuthUser authUser = converter.from(jwt);

        assertThat(authUser).isEqualTo(new AuthUser(IDENTITY_SUB, EMAIL, DISPLAY_NAME));
    }

    @Test
    void shouldUsePreferredUsernameWhenNameIsMissing() {
        Jwt jwt = jwt(Map.of(
                "sub", IDENTITY_SUB,
                "email", EMAIL,
                "preferred_username", DISPLAY_NAME
        ));

        AuthUser authUser = converter.from(jwt);

        assertThat(authUser.displayName()).isEqualTo(DISPLAY_NAME);
    }

    @Test
    void shouldUseEmailWhenDisplayNameClaimsAreMissing() {
        Jwt jwt = jwt(Map.of(
                "sub", IDENTITY_SUB,
                "email", EMAIL
        ));

        AuthUser authUser = converter.from(jwt);

        assertThat(authUser.displayName()).isEqualTo(EMAIL);
    }

    @Test
    void shouldRejectJwtWithoutEmail() {
        Jwt jwt = jwt(Map.of(
                "sub", IDENTITY_SUB
        ));

        assertThatThrownBy(() -> converter.from(jwt))
                .isInstanceOf(InvalidAuthenticationClaimsException.class)
                .hasMessage("Authenticated JWT is missing required claim: email");
    }
}
