package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.event.validate.ErrorCustomFuture;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Глобальный обработчик исключений для REST API.
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter FORMAT_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Преобразует stack trace исключения в строку.
     *
     * @param ex исключение
     * @return stack trace в виде строки
     */
    private String convertStackTraceToString(Exception ex) {
        Writer stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * Обрабатывает исключения ненайденных ресурсов.
     *
     * @param ex исключение NotFoundResource
     * @return объект ошибки API
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundResource(NotFoundResource ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Запрашиваемый объект не найден")
                .status(HttpStatus.NOT_FOUND.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает исключения конфликтов ресурсов.
     *
     * @param ex исключение ConflictResource
     * @return объект ошибки API
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictResource(ConflictResource ex) {
        log.error("Resource conflict: {}", ex.getMessage());
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Нарушено ограничение целостности")
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает исключения валидации даты.
     *
     * @param ex исключение ErrorCustomFuture
     * @return объект ошибки API
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleErrorCustomFuture(ErrorCustomFuture ex) {
        log.error("Date validation error: {}", ex.getMessage());
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Нарушено ограничение целостности")
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает исключения валидации аргументов методов.
     *
     * @param ex исключение MethodArgumentNotValidException
     * @return объект ошибки API
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiError handleArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Argument validation error: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ex.getMessage());

        return ApiError.builder()
                .message(message)
                .reason("Некорректные параметры")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает исключения нарушений ограничений.
     *
     * @param ex исключение ConstraintViolationException
     * @return объект ошибки API
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiError handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Некорректные параметры")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает исключения некорректных запросов.
     *
     * @param ex исключение BadRequestException
     * @return объект ошибки API
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiError handleBadRequestException(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Некорректные параметры")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает исключения отсутствующих параметров запроса.
     *
     * @param ex исключение MissingServletRequestParameterException
     * @return объект ошибки API
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.error("Missing parameter: {}", ex.getMessage());
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Отсутствует параметр")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает исключения отсутствия полномочий.
     *
     * @param ex исключение ForbiddenResource
     * @return объект ошибки API
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenResource(ForbiddenResource ex) {
        log.error("Missing parameter: {}", ex.getMessage());
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Доступ запрещен")
                .status(HttpStatus.FORBIDDEN.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }

    /**
     * Обрабатывает все неперехваченные исключения.
     *
     * @param ex исключение
     * @return объект ошибки API
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ApiError.builder()
                .message("Внутренняя ошибка сервера")
                .reason(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .timestamp(LocalDateTime.now().format(FORMAT_DATE_TIME))
                .build();
    }
}