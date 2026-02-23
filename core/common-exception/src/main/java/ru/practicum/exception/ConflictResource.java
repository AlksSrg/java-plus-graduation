package ru.practicum.exception;

import lombok.Getter;

/**
 * Исключение, выбрасываемое при конфликте данных.
 * Соответствует HTTP статусу 409 Conflict.
 */
@Getter
public class ConflictResource extends RuntimeException {

    private String conflictReason;
    private String conflictingValue;

    /**
     * Создает исключение с сообщением об ошибке.
     *
     * @param message детальное сообщение об ошибке
     */
    public ConflictResource(String message) {
        super(message);
        this.conflictReason = null;
        this.conflictingValue = null;
    }

    /**
     * Создает исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина исключения
     */
    public ConflictResource(String message, Throwable cause) {
        super(message, cause);
        this.conflictReason = null;
        this.conflictingValue = null;
    }

    /**
     * Создает исключение с указанием причины конфликта.
     *
     * @param conflictReason причина конфликта
     * @param message        сообщение об ошибке
     * @return экземпляр ConflictResource
     */
    public static ConflictResource ofReason(String conflictReason, String message) {
        ConflictResource ex = new ConflictResource(message);
        ex.conflictReason = conflictReason;
        return ex;
    }

    /**
     * Создает исключение с указанием конфликтующего значения.
     *
     * @param message          сообщение об ошибке
     * @param conflictingValue конфликтующее значение
     * @return экземпляр ConflictResource
     */
    public static ConflictResource ofValue(String message, String conflictingValue) {
        ConflictResource ex = new ConflictResource(message);
        ex.conflictingValue = conflictingValue;
        return ex;
    }
}