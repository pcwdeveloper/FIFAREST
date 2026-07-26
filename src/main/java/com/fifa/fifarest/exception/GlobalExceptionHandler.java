package com.fifa.fifarest.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(InvalidRefreshTokenException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiError> handleAuthentication(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    @ExceptionHandler({AccessDeniedException.class, ForbiddenActionException.class})
    public ResponseEntity<ApiError> handleAccessDenied(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "This item cannot be modified because it is still referenced by other data", request, null);
    }

    @ExceptionHandler(SlotUnavailableException.class)
    public ResponseEntity<ApiError> handleSlotUnavailable(SlotUnavailableException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ApiError> handlePaymentGateway(PaymentGatewayException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request,
                                            Map<String, String> fieldErrors) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(error);
    }
}
