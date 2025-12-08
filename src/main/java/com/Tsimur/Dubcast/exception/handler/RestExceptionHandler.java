package com.Tsimur.Dubcast.exception.handler;

import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.Tsimur.Dubcast.exception.type.EmailAlreadyUsedException;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.exception.type.SlotCurrentlyPlayingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "com.Tsimur.Dubcast.controller.api")
public class RestExceptionHandler {

    @ExceptionHandler(SlotCurrentlyPlayingException.class)
    public ResponseEntity<ErrorResponse> handleSlotCurrentlyPlaying(
            SlotCurrentlyPlayingException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }


    // ======= ВАЛИДАЦИЯ =======
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (m1, m2) -> m2
                ));

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    // ======= ДУБЛИКАТЫ / КОНФЛИКТЫ =======
    @ExceptionHandler({DataIntegrityViolationException.class, EmailAlreadyUsedException.class})
    public ResponseEntity<ErrorResponse> handleDuplicate(
            Exception ex,
            HttpServletRequest request
    ) {
        log.warn("Duplicate data: {}", ex.getMessage(), ex);
        return build(HttpStatus.CONFLICT, "User with this email already exists", request);
    }

    // ======= 404 =======
    @ExceptionHandler({NotFoundException.class, EntityNotFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNotFound(
            Exception ex,
            HttpServletRequest request
    ) {
        log.info("Not found {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        String message = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Resource not found"
                : ex.getMessage();
        return build(HttpStatus.NOT_FOUND, message, request);
    }

    // ======= ЛЮБЫЕ ПРОЧИЕ ОШИБКИ (500) =======
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(
            Exception ex,
            HttpServletRequest request
    ) {
        // ВАЖНО: здесь логируем ПОЛНЫЙ стек
        log.error("Unexpected exception on {} {}",
                request.getMethod(), request.getRequestURI(), ex);

        String message = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Internal server error"
                : ex.getMessage();

        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    // ======= ВСПОМОГАТЕЛЬНЫЙ МЕТОД =======
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest req
    ) {
        if (message == null || message.isBlank()) {
            message = status.getReasonPhrase();
        }

        ErrorResponse body = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
