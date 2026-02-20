package ru.practicum.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.practicum.exception.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для всех микросервисов.
 * Обеспечивает единообразный формат ответов при ошибках.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка исключений типа NotFoundResource (404).
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с ошибкой
     */
    @ExceptionHandler(NotFoundResource.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundResource ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.toString())
                .message(ex.getMessage())
                .reason("The required object was not found.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка исключений типа ConflictResource (409).
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с ошибкой
     */
    @ExceptionHandler(ConflictResource.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictResource ex, WebRequest request) {
        log.error("Conflict occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.toString())
                .message(ex.getMessage())
                .reason("Integrity constraint has been violated.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Обработка исключений типа ValidationException (400).
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с ошибкой
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .message(ex.getMessage())
                .reason("Incorrectly made request.")
                .errors(ex.getErrors())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключений типа ForbiddenException (403).
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с ошибкой
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, WebRequest request) {
        log.error("Forbidden: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.toString())
                .message(ex.getMessage())
                .reason("Access denied.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Обработка всех остальных исключений (500).
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с ошибкой
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaught(Exception ex, WebRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .message(ex.getMessage())
                .reason("Error occurred on the server.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Обработка исключений валидации аргументов метода (400).
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с ошибкой
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .message("Validation failed")
                .reason("Incorrectly made request.")
                .errors(errors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}