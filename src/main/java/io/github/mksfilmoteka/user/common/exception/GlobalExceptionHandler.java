package io.github.mksfilmoteka.user.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, ErrorCode.CONFLICT));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();

        ErrorResponse errorResponse =
                buildResponse("Validation failed", request, ErrorCode.VALIDATION_FAILED, details);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        List<ErrorDetail> errorDetails =
                List.of(new ErrorDetail(ex.getName(), expectedValueMessage(ex.getRequiredType())));

        return ResponseEntity.badRequest()
                .body(buildResponse(message, request, ErrorCode.BAD_REQUEST, errorDetails));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String message = ex.getMessage();
        Throwable cause = ex.getMostSpecificCause();
        List<ErrorDetail> errorDetails = new ArrayList<>();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            String field = extractFieldName(invalidFormatException);
            message = "Invalid value '%s' for field '%s'".formatted(invalidFormatException.getValue(), field);
            errorDetails.add(new ErrorDetail(field, expectedValueMessage(invalidFormatException.getTargetType())));
        }

        return ResponseEntity.badRequest()
                .body(buildResponse(message, request, ErrorCode.BAD_REQUEST, errorDetails));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {

        return ResponseEntity.badRequest()
                .body(buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(InvalidAuthenticationClaimsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuthenticationClaims(
            InvalidAuthenticationClaimsException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, ErrorCode.UNAUTHORIZED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        String message = "Unexpected error occurred";

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request, ErrorCode.INTERNAL_ERROR));
    }

    private ErrorResponse buildResponse(
            String message,
            HttpServletRequest request,
            ErrorCode code,
            List<ErrorDetail> errorDetails) {

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                message,
                request.getRequestURI(),
                code,
                errorDetails);
    }

    private ErrorResponse buildResponse(
            HttpStatus status, String message, HttpServletRequest request, ErrorCode code) {

        return new ErrorResponse(
                LocalDateTime.now(), status.value(), message, request.getRequestURI(), code, List.of());
    }

    private String extractFieldName(InvalidFormatException ex) {
        return ex.getPath()
                .stream()
                .map(JacksonException.Reference::getPropertyName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("requestBody");
    }

    private String expectedValueMessage(Class<?> targetType) {
        if (targetType != null && targetType.isEnum()) {
            return "Allowed values are: " + allowedValues(targetType);
        }

        return "Expected type: " + (targetType == null ? "valid value" : targetType.getSimpleName());
    }

    private String allowedValues(Class<?> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }
}
