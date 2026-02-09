package ru.practicum.exception;

/**
 * Исключение для конфликтов ресурсов (HTTP 409).
 */
public class ConflictResource extends RuntimeException {
    public ConflictResource(String message) {
        super(message);
    }
}