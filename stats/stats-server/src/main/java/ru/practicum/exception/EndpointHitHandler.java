package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Глобальный обработчик исключений для контроллеров статистики эндпоинтов.
 */
@ControllerAdvice
public class EndpointHitHandler {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    /**
     * Обрабатывает исключения при невозможности добавления элемента.
     *
     * @param exception исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler
    public ResponseEntity<ResponseError> unableAddElementExceptionHandler(final UnableAddElementException exception) {
        ResponseError error = ResponseError.builder()
                .errorMessage(exception.getMessage())
                .reason(exception.getReason())
                .timestamp(getTimeStamp())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения валидации аргументов метода.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ResponseError error = ResponseError.builder()
                .errorMessage("Validation error")
                .reason(ex.getBindingResult().getFieldErrors().toString())
                .timestamp(getTimeStamp())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает нарушения ограничений валидации.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseError> handleConstraintViolation(ConstraintViolationException ex) {
        ResponseError error = ResponseError.builder()
                .errorMessage("Constraint violation")
                .reason(ex.getMessage())
                .timestamp(getTimeStamp())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает неверные аргументы.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseError> handleIllegalArgumentException(IllegalArgumentException ex) {
        ResponseError error = ResponseError.builder()
                .errorMessage("Invalid argument")
                .reason(ex.getMessage())
                .timestamp(getTimeStamp())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения при нечитаемом HTTP сообщении.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseError> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        ResponseError error = ResponseError.builder()
                .errorMessage("Invalid request format")
                .reason(ex.getMessage())
                .timestamp(getTimeStamp())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает несоответствие типов аргументов метода.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseError> handleMethodArgumentTypeMismatchException(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        ResponseError error = ResponseError.builder()
                .errorMessage("Invalid parameter type")
                .reason(ex.getMessage())
                .timestamp(getTimeStamp())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private String getTimeStamp() {
        return LocalDateTime.now().format(dateTimeFormatter);
    }
}