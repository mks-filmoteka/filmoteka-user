package io.github.mksfilmoteka.user.util;

import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

public class TestUtil {

    public static final JsonMapper JSON_MAPPER = JsonMapper.builder().findAndAddModules().build();

    public static Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(existingClaims -> existingClaims.putAll(claims))
                .build();
    }

}