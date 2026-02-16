package ru.practicum.exception;

import lombok.Getter;

/**
 * Исключение, выбрасываемое при неверном запросе.
 * Соответствует HTTP статусу 400 Bad Request.
 */
@Getter
public class BadRequestException extends RuntimeException {

    private final String parameter;

    /**
     * Создает исключение с сообщением об ошибке.
     *
     * @param message детальное сообщение об ошибке
     */
    public BadRequestException(String message) {
        super(message);
        this.parameter = null;
    }

    /**
     * Создает исключение с указанием параметра, вызвавшего ошибку.
     *
     * @param parameter параметр запроса
     * @param message   сообщение об ошибке
     */
    public BadRequestException(String parameter, String message) {
        super(message);
        this.parameter = parameter;
    }
}