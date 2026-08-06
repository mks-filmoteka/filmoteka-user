package io.github.mksfilmoteka.user.auth;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

public final class AuthTestData {

    public static final String IDENTITY_SUB = "identity-123";
    public static final String EMAIL = "user@filmoteka.local";
    public static final String DISPLAY_NAME = "Test User";

    private AuthTestData() {
    }

    public static Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(existingClaims -> existingClaims.putAll(claims))
                .build();
    }
}
