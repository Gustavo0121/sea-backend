package com.sea.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Handles exceptions that reach the DispatcherServlet. Failures raised earlier in the security
 * filter chain (missing/invalid token, insufficient role) are handled by RestAuthErrorHandler instead,
 * since they never reach a controller. Business/validation exceptions from later phases are added here.
 *
 * All handlers log only the exception type/path/status, never the request body or ex.getMessage()
 * for parsing failures — Jackson can echo a snippet of the offending payload (e.g. a login "senha")
 * in HttpMessageNotReadableException#getMessage(), so that one is deliberately not logged verbatim.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Falha de autenticação em {}.", path(request));
        return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.info("Dados inválidos recebidos em {}.", path(request));
        return build(HttpStatus.BAD_REQUEST, message.isEmpty() ? "Dados inválidos." : message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        log.info("Corpo da requisição ausente ou malformado em {}.", path(request));
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou malformado.", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.info("Dados inválidos recebidos em {}.", path(request));
        return build(HttpStatus.BAD_REQUEST, message.isEmpty() ? "Dados inválidos." : message, request);
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleCepNaoEncontradoException(CepNaoEncontradoException ex, WebRequest request) {
        log.info("CEP não encontrado em {}.", path(request));
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ViaCepIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleViaCepIndisponivelException(ViaCepIndisponivelException ex, WebRequest request) {
        log.warn("ViaCEP indisponível ao processar {}.", path(request));
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(CpfDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleCpfDuplicadoException(CpfDuplicadoException ex, WebRequest request) {
        log.info("Tentativa de cadastro com CPF duplicado em {}.", path(request));
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleClienteNaoEncontradoException(ClienteNaoEncontradoException ex, WebRequest request) {
        log.info("Cliente não encontrado em {}.", path(request));
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Erro interno inesperado em {}.", path(request), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        ErrorResponse body = new ErrorResponse(status.value(), message, path(request));
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
