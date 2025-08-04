package com.marcicomentariosfacebook.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Error genérico (última línea de defensa)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("❌ Error inesperado", ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, exchange);
    }

    // Error de parámetros o input inválido (ej. query param incorrecto)
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> handleInputException(ServerWebInputException ex, ServerWebExchange exchange) {
        log.warn("⚠️ Error de entrada", ex);
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, exchange);
    }

    // Error de validación de objetos @Valid (binding)
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        log.warn("⚠️ Error de validación de entrada", ex);

        // Crear mensaje con todos los errores de campo concatenados
        String errors = ex.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errors)
                .path(exchange.getRequest().getPath().value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Error de WebClient al hacer llamadas a APIs externas
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> handleWebClientResponse(WebClientResponseException ex, ServerWebExchange exchange) {
        log.error("🌐 Error de llamada externa", ex);
        return buildErrorResponse(ex, HttpStatus.valueOf(ex.getRawStatusCode()), exchange);
    }

    // Método genérico para construir el error en formato estándar
    private ResponseEntity<ApiError> buildErrorResponse(Throwable ex, HttpStatus status, ServerWebExchange exchange) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor")
                .path(exchange.getRequest().getPath().value())
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
