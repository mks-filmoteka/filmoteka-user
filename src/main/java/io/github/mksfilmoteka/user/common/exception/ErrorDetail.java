package io.github.mksfilmoteka.user.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error")
public record ErrorDetail(
        @Schema(description = "invalid field", example = "name")
        String field,

        @Schema(description = "error message", example = "must not be blank")
        String message
) {
}
