package com.sea.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sea.backend.exception.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Converts security failures raised inside the filter chain (before any @ControllerAdvice
 * can see them) into the same JSON error shape used elsewhere in the API, with no stacktrace.
 * Logs only method/path — never the Authorization header or token contents.
 */
@Component
public class RestAuthErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RestAuthErrorHandler.class);

    private final ObjectMapper objectMapper;

    public RestAuthErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        log.warn("Requisição não autenticada: {} {}.", request.getMethod(), request.getRequestURI());
        writeError(request, response, HttpStatus.UNAUTHORIZED, "Credenciais inválidas ou token ausente/expirado.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Acesso negado: {} {}.", request.getMethod(), request.getRequestURI());
        writeError(request, response, HttpStatus.FORBIDDEN, "Acesso negado para este recurso.");
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response,
                             HttpStatus status, String message) throws IOException {
        ErrorResponse body = new ErrorResponse(status.value(), message, request.getRequestURI());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
