package ru.student.mediaportal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpectedError(Exception ex) {
        logger.error("Необработанная ошибка", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", Map.of());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> notFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> conflict(DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT, "Данные нарушают ограничения базы", Map.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Ошибка валидации", errors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Некорректный JSON или формат поля", Map.of());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(new ApiError(Instant.now(), status.value(),
                "Ошибка HTTP " + status.value(), Map.of()), headers, status);
    }
    private ResponseEntity<Object> error(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), message, errors));
    }
}
