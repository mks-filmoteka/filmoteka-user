package io.github.mksfilmoteka.user.auth;

public record AuthUser(
        String identitySub,
        String email,
        String displayName
) {
}
