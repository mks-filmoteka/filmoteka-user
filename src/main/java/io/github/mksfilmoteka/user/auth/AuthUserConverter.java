package io.github.mksfilmoteka.user.auth;

import io.github.mksfilmoteka.user.common.exception.InvalidAuthenticationClaimsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class AuthUserConverter {

    private static final String EMAIL_CLAIM = "email";
    private static final String NAME_CLAIM = "name";
    private static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

    public AuthUser from(Jwt jwt) {
        String identitySub = getRequiredClaim(jwt, "sub");
        String email = getRequiredClaim(jwt, EMAIL_CLAIM);

        String displayName = Stream.of(jwt.getClaimAsString(NAME_CLAIM),
                        jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM), email)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseThrow();

        return new AuthUser(identitySub, email, displayName);
    }

    private static String getRequiredClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);

        if (value == null || value.isBlank()) {
            throw new InvalidAuthenticationClaimsException(claimName);
        }

        return value;
    }
}
