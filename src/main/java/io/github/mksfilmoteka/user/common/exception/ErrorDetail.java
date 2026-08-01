package io.github.mksfilmoteka.user.common.exception;

public record ErrorDetail(
        String field,
        String message
) {
}
