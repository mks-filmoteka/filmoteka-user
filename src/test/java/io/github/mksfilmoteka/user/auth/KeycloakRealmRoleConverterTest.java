package io.github.mksfilmoteka.user.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.github.mksfilmoteka.user.auth.AuthTestData.jwt;
import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void shouldConvertScopesAndRealmRoles() {
        Jwt jwt = jwt(Map.of(
                "scope", "openid profile",
                "realm_access", Map.of("roles", List.of("USER", "ADMIN")))
        );
        Collection<GrantedAuthority> result = converter.convert(jwt);
        assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("SCOPE_openid", "SCOPE_profile", "ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnScopeAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = jwt(Map.of("scope", "openid profile"));
        Collection<GrantedAuthority> result = converter.convert(jwt);
        assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("SCOPE_openid", "SCOPE_profile");
    }

    @Test
    void shouldReturnScopeAuthoritiesWhenRolesClaimIsMissing() {
        Jwt jwt = jwt(Map.of("scope", "openid", "realm_access", Map.of()));
        Collection<GrantedAuthority> result = converter.convert(jwt);
        assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("SCOPE_openid");
    }
}