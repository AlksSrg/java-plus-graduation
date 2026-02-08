package ru.practicum.exception;

/**
 * Исключение для ненайденных ресурсов (HTTP 404).
 */
public class NotFoundResource extends RuntimeException {
    public NotFoundResource(String message) {
        super(message);
    }
}