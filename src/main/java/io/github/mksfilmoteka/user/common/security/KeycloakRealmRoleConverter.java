package io.github.mksfilmoteka.user.common.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> scopeAuthorities = scopeAuthoritiesConverter.convert(jwt);
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopeAuthorities);
        Object realmAccessClaim = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)) {
            return authorities;
        }
        Object rolesClaim = realmAccess.get(ROLES_CLAIM);
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return authorities;
        }
        roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .forEach(authorities::add);
        return authorities;
    }
}
