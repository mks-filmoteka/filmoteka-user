package io.github.mksfilmoteka.user.auth;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

public final class AuthTestData {

    public static Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(existingClaims -> existingClaims.putAll(claims))
                .build();
    }
}
