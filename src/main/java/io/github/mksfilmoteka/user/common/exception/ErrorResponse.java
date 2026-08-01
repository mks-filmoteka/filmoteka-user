package io.github.mksfilmoteka.user.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path,
        ErrorCode code,
        List<ErrorDetail> errorDetails
) {
}
