package io.github.mksfilmoteka.user.common.exception;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response")
public record ErrorResponse(
        @Schema(description = "timestamp")
        LocalDateTime timestamp,

        @Schema(description = "http status", example = "404")
        int status,

        @Schema(description = "error message", example = "Film list with id 1 not found")
        String message,

        @Schema(description = "method uri", example = "/api/v1/film-lists/1")
        String path,

        @Schema(description = "error code", example = "NOT_FOUND")
        ErrorCode code,

        @ArraySchema(schema = @Schema(implementation = ErrorDetail.class))
        List<ErrorDetail> errorDetails
) {
}
