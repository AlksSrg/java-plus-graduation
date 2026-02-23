package ru.practicum.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Исключение, выбрасываемое при ошибках валидации данных.
 * Соответствует HTTP статусу 400 Bad Request.
 */
@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    /**
     * Создает исключение с сообщением об ошибке.
     *
     * @param message детальное сообщение об ошибке
     */
    public ValidationException(String message) {
        super(message);
        this.errors = null;
    }

    /**
     * Создает исключение с детальными ошибками валидации по полям.
     *
     * @param message общее сообщение
     * @param errors  карта ошибок (поле -> сообщение об ошибке)
     */
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    /**
     * Создает исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина исключения
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = null;
    }
}