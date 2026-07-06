package com.sea.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Handles exceptions that reach the DispatcherServlet. Failures raised earlier in the security
 * filter chain (missing/invalid token, insufficient role) are handled by RestAuthErrorHandler instead,
 * since they never reach a controller. Business/validation exceptions from later phases are added here.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas.",
                path(request)
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message.isEmpty() ? "Dados inválidos." : message,
                path(request)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno inesperado.",
                path(request)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String path(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
