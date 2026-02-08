package ru.practicum.event.validate;

import jakarta.validation.ConstraintDeclarationException;
import lombok.Getter;
import ru.practicum.exception.ConflictResource;

/**
 * Исключение для ошибок валидации даты события.
 */
@Getter
public class ErrorCustomFuture extends ConstraintDeclarationException {
    /**
     * -- GETTER --
     * Возвращает конфликт ресурса.
     *
     */
    private final ConflictResource conflictResource;

    /**
     * Создает исключение с конфликтом ресурса.
     *
     * @param conflictResource конфликт ресурса
     */
    public ErrorCustomFuture(ConflictResource conflictResource) {
        super();
        this.conflictResource = conflictResource;
    }

    /**
     * Создает исключение с конфликтом ресурса и сообщением.
     *
     * @param conflictResource конфликт ресурса
     * @param message          сообщение об ошибке
     * @param args             аргументы для форматирования сообщения
     */
    public ErrorCustomFuture(ConflictResource conflictResource, String message, Object... args) {
        super(String.format(message, args));
        this.conflictResource = conflictResource;
    }
}