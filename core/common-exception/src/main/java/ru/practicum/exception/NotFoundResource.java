package ru.practicum.exception;

import lombok.Getter;

/**
 * Исключение, выбрасываемое при попытке обращения к несуществующему ресурсу.
 * Соответствует HTTP статусу 404 Not Found.
 */
@Getter
public class NotFoundResource extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    /**
     * Создает исключение с сообщением об ошибке.
     *
     * @param message детальное сообщение об ошибке
     */
    public NotFoundResource(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    /**
     * Создает исключение с указанием типа ресурса и его идентификатора.
     *
     * @param resourceType тип ресурса (например, "user", "event", "category")
     * @param resourceId   идентификатор ресурса
     */
    public NotFoundResource(String resourceType, String resourceId) {
        super(String.format("%s с id=%s не найден", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Создает исключение с указанием типа ресурса и его идентификатора (Long версия).
     *
     * @param resourceType тип ресурса
     * @param resourceId   идентификатор ресурса
     */
    public NotFoundResource(String resourceType, Long resourceId) {
        this(resourceType, String.valueOf(resourceId));
    }

    /**
     * Создает исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина исключения
     */
    public NotFoundResource(String message, Throwable cause) {
        super(message, cause);
        this.resourceType = null;
        this.resourceId = null;
    }
}