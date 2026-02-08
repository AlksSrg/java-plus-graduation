package ru.practicum.exception;

/**
 * Исключение для некорректных запросов (HTTP 400).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}