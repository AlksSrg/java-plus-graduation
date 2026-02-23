package ru.practicum.exception;

/**
 * Исключение, выбрасываемое при отсутствии прав на выполнение операции.
 * Соответствует HTTP статусу 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Создает исключение с сообщением об ошибке.
     *
     * @param message детальное сообщение об ошибке
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Создает исключение с указанием причины.
     *
     * @param resourceType тип ресурса
     * @param action       действие
     */
    public ForbiddenException(String resourceType, String action) {
        super(String.format("Доступ запрещен: действие '%s' над ресурсом '%s' не разрешено", action, resourceType));
    }
}