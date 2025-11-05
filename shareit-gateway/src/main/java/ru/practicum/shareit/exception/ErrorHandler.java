package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> error = new HashMap<>();
        var fieldError = e.getBindingResult().getFieldError();
        if (fieldError != null) {
            error.put("error", "Ошибка валидации: " + fieldError.getDefaultMessage());
        } else {
            error.put("error", "Ошибка валидации");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> handleResourceAccessException(ResourceAccessException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Сервер недоступен: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> handleRestClientException(RestClientException e) {
        if (e instanceof ResourceAccessException resourceAccessException) {
            return handleResourceAccessException(resourceAccessException);
        }
        Map<String, String> error = new HashMap<>();
        String message = e.getMessage();
        error.put("error", "Ошибка при обращении к серверу: " + (message != null ? message : "Неизвестная ошибка"));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        Throwable cause = e.getCause();
        if (cause instanceof ResourceAccessException resourceAccessException) {
            return handleResourceAccessException(resourceAccessException);
        }
        if (cause instanceof RestClientException restClientException) {
            return handleRestClientException(restClientException);
        }
        String message = e.getMessage();
        error.put("error", "Ошибка при обращении к серверу: " + (message != null ? message : "Неизвестная ошибка"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Внутренняя ошибка сервера: " + e.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}


